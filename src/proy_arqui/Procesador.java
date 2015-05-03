package proy_arqui;

public class Procesador extends Thread {
    
    private static Multiprocesador myMp;
    
    //COLUMNAS EN CACHE
    private final int ID = 0;
    private final int EST = 1;
    
    //ESTADOS DE BLOQUES
    private final int C = 0;
    private final int M = 1;
    private final int I = 2;
    
    private int PC; //contador de programa
    private int IR; //registro de instruccion
    private int regs[] = new int[32]; //32 registros
    //para la cache de datos agregamos dos filas extra que hacen referencia al #deBloque y al #estadoBloque ('C','M','I')
    private int dcache[][] = new int[4][4]; //cache de datos (4 bloques, cada bloque con 4 palabras, cada palabra 4 bytes)
    private int estCache[][] = new int[4][2]; 
    //8bloques*4 = 32 palabras ---> 32palabras*4 = 128 direcciones de palabras
    private int dmem[] = new int[32]; // memoria de datos compartida (8 bloques, cada uno con 4 palabras, cada palabra 4 bytes)
    
    public Procesador(Multiprocesador mp){
        myMp = mp;
        PC = 0;
        for(int x=0; x < 4; ++x){
            estCache[x][EST] = I;
        }
    }
    
    //RX, n(RY)
    //Rx <- M(n + (Ry))
    public void LW(int Y, int X, int n){
        int numByte = regs[Y]+n; //#byte 
        int numBloqMem = Math.floorDiv(numByte,16); //indiceBloqueMemDatos (0-24)
        int numpalabra = (numByte%16)/4;
        int dirBloqCache = numBloqMem%4; //indiceBloqueCache
        int idBloqEnCache = estCache[dirBloqCache][ID]; //bloque que ocupa actualmente esa dir de cache
        int estadoBloqEnCache = estCache[dirBloqCache][EST]; //estado del bloque que ocupa esa dir de cache ('M', 'C', 'I')
        //si el bloque que requerimos no esta en cache
        int convNumBloqMem = numBloqMem*4; //VEREMOS!
        if(idBloqEnCache != convNumBloqMem){
            switch(estadoBloqEnCache){
                case C:
                    //nos traemos el bloque de memoria a cache
                    int j = convNumBloqMem;
                    for(int i = 0; i < 4; i++){
                        dcache[dirBloqCache][i] = dmem[j];
                        j++;
                    }
                    estCache[dirBloqCache][ID] = convNumBloqMem; //bloque que ocupa actualmente esa dir de cache
                    estCache[dirBloqCache][EST] = C; //bloque que ocupa actualmente esa dir de cache
                break;
                case M:
                    j = convNumBloqMem;
                    for(int i = 0; i < 4; i++){
                        dmem[j] = dcache[dirBloqCache][i];
                        j++;
                    }
                    j = convNumBloqMem;
                    for(int i = 0; i < 4; i++){
                        dcache[dirBloqCache][i] = dmem[j];
                        j++;
                    }
                    estCache[dirBloqCache][ID] = convNumBloqMem; //bloque que ocupa actualmente esa dir de cache
                    estCache[dirBloqCache][EST] = C; //bloque que ocupa actualmente esa dir de cache
                break;
                case I:
                    j = convNumBloqMem;
                    for(int i = 0; i < 4; i++){
                        dcache[dirBloqCache][i] = dmem[j];
                        j++;
                    }
                    estCache[dirBloqCache][ID] = convNumBloqMem; //bloque que ocupa actualmente esa dir de cache
                    estCache[dirBloqCache][EST] = C; //bloque que ocupa actualmente esa dir de cache
                    //previsto para los directorios, por el momento no puede estar invalido
                break;
            }
        } //HIT :D
        switch(estadoBloqEnCache){
            case C:
                regs[X] = dcache[dirBloqCache][numpalabra];
            break;
            case M:
                int j = convNumBloqMem;
                for(int i = 0; i < 4; i++){
                    dmem[j] = dcache[dirBloqCache][i];
                    j++;
                }
                estCache[dirBloqCache][ID] = convNumBloqMem; //bloque que ocupa actualmente esa dir de cache
                estCache[dirBloqCache][EST] = C; //bloque que ocupa actualmente esa dir de cache

                regs[X] = dcache[dirBloqCache][numpalabra];
            break;
            case I:
                //previsto para los directorios, por el momento no puede estar invalido
            break;
        }
    }
    
    //RX, n(RY)
    //M(n + (Ry))  Rx
    public void SW(int Y, int X, int n){
        System.out.println("valor del registro Y "+ regs[Y] + "y este es el valor de n" +n);
        int numByte = regs[Y]+n; //#byte 
        System.out.println("viendo desplazamiento "+ numByte + "y este es el valor de n" +n);
        int numBloqMem = numByte/16; //indiceBloqueMemDatos (0-24)
        int numpalabra = (numByte%16)/4;
        System.out.println("calculando posiciones en cache "+ numBloqMem + "y este es el valor de n" +n);
        int dirBloqCache = numBloqMem%4; //indiceBloqueCache
        int idBloqEnCache = estCache[dirBloqCache][ID]; //bloque que ocupa actualmente esa dir de cache
        int estadoBloqEnCache = estCache[dirBloqCache][EST]; //estado del bloque que ocupa esa dir de cache ('M', 'C', 'I')
        //si el bloque que requerimos no esta en cache
        int convNumBloqMem = numBloqMem*4; //VEREMOS!
        if(idBloqEnCache != convNumBloqMem){
            switch(estadoBloqEnCache){
                case C:
                    //nos traemos el bloque de memoria a cache
                    int j = convNumBloqMem;
                    for(int i = 0; i < 4; i++){
                        dcache[dirBloqCache][i] = dmem[j];
                        j++;
                    }
                    estCache[dirBloqCache][ID] = convNumBloqMem; //bloque que ocupa actualmente esa dir de cache
                    estCache[dirBloqCache][EST] = C; //bloque que ocupa actualmente esa dir de cache
                break;
                case M:
                    j = convNumBloqMem;
                    for(int i = 0; i < 4; i++){
                        dmem[j] = dcache[dirBloqCache][i];
                        j++;
                    }
                    j = convNumBloqMem;
                    for(int i = 0; i < 4; i++){
                        dcache[dirBloqCache][i] = dmem[j];
                        j++;
                    }
                    estCache[dirBloqCache][ID] = convNumBloqMem; //bloque que ocupa actualmente esa dir de cache
                    estCache[dirBloqCache][EST] = C; //bloque que ocupa actualmente esa dir de cache
                break;
                case I:
                    j = convNumBloqMem;
                    for(int i = 0; i < 4; i++){
                        dcache[dirBloqCache][i] = dmem[j];
                        j++;
                    }
                    estCache[dirBloqCache][ID] = convNumBloqMem; //bloque que ocupa actualmente esa dir de cache
                    estCache[dirBloqCache][EST] = C; //bloque que ocupa actualmente esa dir de cache
                    //previsto para los directorios, por el momento no puede estar invalido
                break;
            }
        } //HIT :D
        switch(estadoBloqEnCache){
            case C:
                dcache[dirBloqCache][numpalabra] = regs[X];
                estCache[dirBloqCache][ID] = convNumBloqMem; //bloque que ocupa actualmente esa dir de cache
                estCache[dirBloqCache][EST] = M; //bloque que ocupa actualmente esa dir de cache
            break;
            case M:
                int j = convNumBloqMem;
                for(int i = 0; i < 4; i++){
                    dmem[j] = dcache[dirBloqCache][i];
                    j++;
                }
                estCache[dirBloqCache][ID] = convNumBloqMem; //bloque que ocupa actualmente esa dir de cache
                estCache[dirBloqCache][EST] = C; //bloque que ocupa actualmente esa dir de cache
                    
                dcache[dirBloqCache][numpalabra] = regs[X];
                estCache[dirBloqCache][ID] = convNumBloqMem; //bloque que ocupa actualmente esa dir de cache
                estCache[dirBloqCache][EST] = M; //bloque que ocupa actualmente esa dir de cache
            break;
            case I:
                    //previsto para los directorios, por el momento no puede estar invalido
            break;
        }
    }
    public void BEQZ(int X, int n){
        if(regs[X]==0) PC+=4*(n-1);
    }
    public void BNEZ(int X, int n){
        if(regs[X]!=0) PC+=4*(n-1);
    }
    public void DADDI(int Y, int X, int n){
        regs[X]=regs[Y]+n;
        System.out.println("Estoy intentando meter en el registro "+X+" lo que está en el registro "+Y+" "+n);
    }
    public void DADD(int Y, int Z, int X){
        regs[X]=regs[Y]+regs[Z];
    }
    public void DSUB(int Y, int Z, int X){
        regs[X]=regs[Y]-regs[Z];
    }
    
    public void procesarInstruccion(int cod, int param1, int param2, int param3){
        PC = PC + 4;
        switch(cod){
            case 8:
                DADDI(param1, param2, param3);
            break;
            case 32:
                DADD(param1, param2, param3);
            break;
            case 34:
                DSUB(param1, param2, param3);
            break;
            case 35:
                LW(param1, param2, param3);
            break;
            case 43:
                SW(param1, param2, param3);
            break;
            case 4:
                BEQZ(param1, param3);
            break;
            case 5:
                BNEZ(param1, param3);
            break;
            case 63:
            break;
        }
    }
    
    public void procesar(int pcA, int limit){
        IR = PC;
        int cod, p1, p2, p3;
        for(int i = IR; i < limit; i+=4){
            cod = myMp.getInstIdx(i);
            p1 = myMp.getInstIdx(i+1);
            p2 = myMp.getInstIdx(i+2); 
            p3 = myMp.getInstIdx(i+3);
            procesarInstruccion(cod, p1, p2, p3);
            verEstado();
        }
    }
    
    public void FIN(){}   
    
    public void verEstado(){
        String estado = "";
        estado += "El PC es: "+ PC + "\n";
        estado += "El IR es: "+ IR + "\n";
        estado += "Los registros de procesador son:\n";
        for(int i = 0; i < 32; i++){
            estado += regs[i]+", ";
        }
        estado += "\n";
        estado += "La memoria cache contiene:\n";
        for(int i = 0; i < 4; i++){
            estado+="Bloque "+i+", estado: "+estCache[i][EST]+", idBloque: "+estCache[i][ID]+" --> ";
            for(int j= 0; j < 4; j++){
                 estado += dcache[i][j]+ ", ";
            }
            estado += "\n";
        }
        estado += "La memoria de datos contiene:\n";
        for(int i = 0; i < 32; i++){
            estado += dmem[i]+", ";
        }
        estado += "\n";
        System.out.println(estado);
    }
}
