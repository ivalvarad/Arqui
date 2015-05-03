package proy_arqui;

public class Procesador extends Thread {
    
    private static Multiprocesador myMp;
    
    //COLUMNAS EN CACHE
    private final int ID = 0;
    private final int ESTADO = 1;
    
    //ESTADOS DE BLOQUES
    private final int C = 0;
    private final int M = 1;
    private final int I = 2;
    
    private int PC; // Contador de programa
    private int IR; // Registro de instruccion
    private int registros[] = new int[32];          // 32 registros
    // Para la cache de datos agregamos dos filas extra que hacen referencia al número de bloque y al estado del bloque ('C','M','I')
    private int cacheDatos[][] = new int[4][4];     // Cache de datos (4 bloques, cada bloque con 4 palabras, cada palabra 4 bytes)
    private int estadoCache[][] = new int[4][2];    // 8bloques*4 = 32 palabras ---> 32palabras*4 = 128 direcciones de palabras
    private int memoria[] = new int[32];            // Memoria de datos compartida (8 bloques, cada uno con 4 palabras)
    
    public Procesador(Multiprocesador mp){
        myMp = mp;
    }
    
    public void cargarACache(int direccionMemoria, int direccionCache){
        int j = direccionMemoria;
        int i;
        // Copia el bloque entero en el lugar que le corresponde en cache
        for(i = 0; i < 4; i++){
            cacheDatos[direccionCache][i] = memoria[j];
            j++;
        }
    }
    
    public void guardarEnMemoria(int direccionMemoria, int direccionCache){
        int j = direccionMemoria;
        for(int i = 0; i < 4; i++){
            memoria[j] = cacheDatos[direccionCache][i];
            j++;
        }
    }

    // Cargar una palabra: RX, n(RY)
    // Rx <- M(n + (Ry))
    public void LW(int Y, int X, int n){
        int numByte = registros[Y]+n;                               // Numero del byte que quiero leer de memoria 
        int numBloqMem = Math.floorDiv(numByte,16);                 // Indice del bloque en memoria (0-24)
        int numPalabra = (numByte%16)/4;
        int dirBloqCache = numBloqMem%4;                            // Indice donde debe estar el bloque en cache
        int idBloqEnCache = estadoCache[dirBloqCache][ID];          // ID del bloque que ocupa actualmente esa direccion en cache
        int estadoBloqEnCache = estadoCache[dirBloqCache][ESTADO];  //estado del bloque que ocupa esa dir de cache ('M', 'C', 'I')
        
        // Si el bloque que requerimos no esta en cache:
        int dirNumBloqMem = numBloqMem*4;                           // Conversion para mapear la direccion inicial del bloque en memoria
        if(idBloqEnCache != /*numBloqMem*/ dirNumBloqMem){          // El ID que se guarda en caché es el mismo de memoria. -Érick
            switch(estadoBloqEnCache){
                case C:         // Si está compartido nos traemos el bloque de memoria a cache
                    cargarACache(dirNumBloqMem, dirBloqCache);
                    estadoCache[dirBloqCache][ID] = dirNumBloqMem;  // Bloque que ocupa ahora esa direccion de cache
                    estadoCache[dirBloqCache][ESTADO] = C;          // Estado del bloque que ocupa ahora esa direccion de cache
                break;
                case M:     // Si está modificado se guarda en memoria el bloque que está en caché y luego se carga el nuevo
                    guardarEnMemoria(idBloqEnCache*4, idBloqEnCache);   // Creo que esos son los parámetros correctos. -Érick
                    /*int j = dirNumBloqMem;
                    for(int i = 0; i < 4; i++){
                        memoria[j] = cacheDatos[dirBloqCache][i];
                        j++;
                    }*/
                    cargarACache(dirNumBloqMem, dirBloqCache);
                    estadoCache[dirBloqCache][ID] = dirNumBloqMem; //bloque que ocupa actualmente esa dir de cache
                    estadoCache[dirBloqCache][ESTADO] = C; //bloque que ocupa actualmente esa dir de cache
                break;
                case I: break;  // Previsto para los directorios, por el momento no puede estar invalido
            }
        }else{ // HIT :D
            switch(estadoBloqEnCache){
                case C: break;  // Si está compartido solo queda cargar la palabra al registro
                case M:
                    guardarEnMemoria(dirNumBloqMem, dirBloqCache);
                    estadoCache[dirBloqCache][ESTADO] = C; //bloque que ocupa actualmente esa dir de cache
                break;
                case I: break;  // Previsto para los directorios, por el momento no puede estar invalido
            }
        }
        // Una vez realizadas las estrategias para hits y misses solo queda cargar la palabra al registro
        registros[X] = cacheDatos[dirBloqCache][numPalabra];
    }
    
    //RX, n(RY)
    //M(n + (Ry))  Rx
    public void SW(int Y, int X, int n){
        int numByte = registros[Y]+n; //#byte 
        int numBloqMem = Math.floorDiv(numByte,16); //indiceBloqueMemDatos (0-24)
        int numpalabra = (numByte%16)/4;
        int dirBloqCache = numBloqMem%4; //indiceBloqueCache
        int idBloqEnCache = estadoCache[dirBloqCache][ID]; //bloque que ocupa actualmente esa dir de cache
        int estadoBloqEnCache = estadoCache[dirBloqCache][ESTADO]; //estado del bloque que ocupa esa dir de cache ('M', 'C', 'I')
        //si el bloque que requerimos no esta en cache
        int convNumBloqMem = numBloqMem*4; //VEREMOS!
        if(idBloqEnCache != convNumBloqMem){
            switch(estadoBloqEnCache){
                case C:
                    //nos traemos el bloque de memoria a cache
                    int j = convNumBloqMem;
                    for(int i = 0; i < 4; i++){
                        cacheDatos[dirBloqCache][i] = memoria[j];
                        j++;
                    }
                    estadoCache[dirBloqCache][ID] = convNumBloqMem; //bloque que ocupa actualmente esa dir de cache
                    estadoCache[dirBloqCache][ESTADO] = C; //bloque que ocupa actualmente esa dir de cache
                break;
                case M:
                    j = convNumBloqMem;
                    for(int i = 0; i < 4; i++){
                        memoria[j] = cacheDatos[dirBloqCache][i];
                        j++;
                    }
                    j = convNumBloqMem;
                    for(int i = 0; i < 4; i++){
                        cacheDatos[dirBloqCache][i] = memoria[j];
                        j++;
                    }
                    estadoCache[dirBloqCache][ID] = convNumBloqMem; //bloque que ocupa actualmente esa dir de cache
                    estadoCache[dirBloqCache][ESTADO] = C; //bloque que ocupa actualmente esa dir de cache
                break;
                case I:
                    //previsto para los directorios, por el momento no puede estar invalido
                break;
            }
        }else{ //HIT :D
            switch(estadoBloqEnCache){
                case C:
                    cacheDatos[dirBloqCache][numpalabra] = registros[X];
                    estadoCache[dirBloqCache][ID] = convNumBloqMem; //bloque que ocupa actualmente esa dir de cache
                    estadoCache[dirBloqCache][ESTADO] = M; //bloque que ocupa actualmente esa dir de cache
                break;
                case M:
                    int j = convNumBloqMem;
                    for(int i = 0; i < 4; i++){
                        memoria[j] = cacheDatos[dirBloqCache][i];
                        j++;
                    }
                    estadoCache[dirBloqCache][ID] = convNumBloqMem; //bloque que ocupa actualmente esa dir de cache
                    estadoCache[dirBloqCache][ESTADO] = C; //bloque que ocupa actualmente esa dir de cache
                    
                    cacheDatos[dirBloqCache][numpalabra] = registros[X];
                    estadoCache[dirBloqCache][ID] = convNumBloqMem; //bloque que ocupa actualmente esa dir de cache
                    estadoCache[dirBloqCache][ESTADO] = M; //bloque que ocupa actualmente esa dir de cache
                break;
                case I:
                    //previsto para los directorios, por el momento no puede estar invalido
                break;
            }
        }
    }
    public void BEQZ(int X, int n){
        if(registros[X]==0) PC+=4*(n-1);
    }
    public void BNEZ(int X, int n){
        if(registros[X]!=0) PC+=4*(n-1);
    }
    public void DADDI(int Y, int X, int n){
        registros[X]=registros[Y]+n;
    }
    public void DADD(int Y, int Z, int X){
        registros[X]=registros[Y]+registros[Z];
    }
    public void DSUB(int Y, int Z, int X){
        registros[X]=registros[Y]-registros[Z];
    }
    public void FIN(){}   
}
