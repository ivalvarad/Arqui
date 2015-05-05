package proy_arqui;

import java.io.IOException;

public class Procesador extends Thread {
    
    private static Multiprocesador myMp;
    
    private int puedoCambiar = 0;
    
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
        for(int x=0; x < 4; ++x){
            estCache[x][EST] = I;
            estCache[x][ID] = -1;
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
        //CASO 1: el bloque que requerimos no esta en cache, en su lugar hay otro bloque
        if(idBloqEnCache != convNumBloqMem){
            //el id del bloque que esta ocupando cache es -1 (no hay bloque) o es otro bloque
            if(idBloqEnCache == -1){
                    int j = convNumBloqMem;
                    for(int i = 0; i < 4; i++){
                        dcache[dirBloqCache][i] = dmem[j];
                        j++;
                    }
                    estCache[dirBloqCache][ID] = convNumBloqMem; //bloque que ocupa actualmente esa dir de cache
                    estCache[dirBloqCache][EST] = C; //bloque que ocupa actualmente esa dir de cache
                    for(int x=0; x<16; ++x){
                        try{
                            Multiprocesador.barrier.await();
                        } catch(IOException e){};
                    }
                    puedoCambiar = 1;
            }else{
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
                        //previsto para los directorios, por el momento no puede estar invalido
                    break;
                }
            }
        }else{ //HIT :D 
            switch(estadoBloqEnCache){
                case C:
                    //regs[X] = dcache[dirBloqCache][numpalabra];
                break;
                case M:
                    int j = convNumBloqMem;
                    for(int i = 0; i < 4; i++){
                        dmem[j] = dcache[dirBloqCache][i];
                        j++;
                    }
                    estCache[dirBloqCache][ID] = convNumBloqMem; //bloque que ocupa actualmente esa dir de cache
                    estCache[dirBloqCache][EST] = C; //bloque que ocupa actualmente esa dir de cache

                    //regs[X] = dcache[dirBloqCache][numpalabra];
                break;
                case I:
                    //previsto para los directorios, por el momento no puede estar invalido
                break;
            }
        }
        regs[X] = dcache[dirBloqCache][numpalabra];
    }
    
    //RX, n(RY)
    //M(n + (Ry))  Rx
    public void SW(int Y, int X, int n){
        int numByte = regs[Y]+n; //#byte 
        int numBloqMem = numByte/16; //indiceBloqueMemDatos (0-24)
        int numpalabra = (numByte%16)/4;
        int dirBloqCache = numBloqMem%4; //indiceBloqueCache
        int idBloqEnCache = estCache[dirBloqCache][ID]; //bloque que ocupa actualmente esa dir de cache
        int estadoBloqEnCache = estCache[dirBloqCache][EST]; //estado del bloque que ocupa esa dir de cache ('M', 'C', 'I')
        //si el bloque que requerimos no esta en cache
        int convNumBloqMem = numBloqMem*4; //VEREMOS!
        //CASO 1: el bloque que requerimos no esta en cache, en su lugar hay otro bloque
        if(idBloqEnCache != convNumBloqMem){
            if(idBloqEnCache==-1){
                int j = convNumBloqMem;
                for(int i = 0; i < 4; i++){
                    dcache[dirBloqCache][i] = dmem[j];
                    j++;
                }
                estCache[dirBloqCache][ID] = convNumBloqMem; //bloque que ocupa actualmente esa dir de cache
                estCache[dirBloqCache][EST] = C; //bloque que ocupa actualmente esa dir de cache
            }else{
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
            }
        }else{//HIT :D
            switch(estadoBloqEnCache){
                case C:
                    /*
                    dcache[dirBloqCache][numpalabra] = regs[X];
                    estCache[dirBloqCache][ID] = convNumBloqMem; //bloque que ocupa actualmente esa dir de cache
                    estCache[dirBloqCache][EST] = M; //bloque que ocupa actualmente esa dir de cache
                     */
                break;
                case M:
                    int j = convNumBloqMem;
                    for(int i = 0; i < 4; i++){
                        dmem[j] = dcache[dirBloqCache][i];
                        j++;
                    }
                    estCache[dirBloqCache][ID] = convNumBloqMem; //bloque que ocupa actualmente esa dir de cache
                    estCache[dirBloqCache][EST] = C; //bloque que ocupa actualmente esa dir de cache

                    /*
                    dcache[dirBloqCache][numpalabra] = regs[X];
                    estCache[dirBloqCache][ID] = convNumBloqMem; //bloque que ocupa actualmente esa dir de cache
                    estCache[dirBloqCache][EST] = M; //bloque que ocupa actualmente esa dir de cache
                     */
                break;
                case I:
                        //previsto para los directorios, por el momento no puede estar invalido
                break;
            }
        }
        dcache[dirBloqCache][numpalabra] = regs[X];
        estCache[dirBloqCache][ID] = convNumBloqMem; //bloque que ocupa actualmente esa dir de cache
        estCache[dirBloqCache][EST] = M; //bloque que ocupa actualmente esa dir de cache
    }
    public void BEQZ(int X, int n){
        if(regs[X]==0) PC+=4*(n-1);
    }
    public void BNEZ(int X, int n){
        if(regs[X]!=0) PC+=4*(n-1);
    }
    public void DADDI(int Y, int X, int n){
        regs[X]=regs[Y]+n;
    }
    public void DADD(int Y, int Z, int X){
        regs[X]=regs[Y]+regs[Z];
    }
    public void DSUB(int Y, int Z, int X){
        regs[X]=regs[Y]-regs[Z];
    }
    
    public void procesarInstruccion(int cod, int param1, int param2, int param3){
        IR = PC;
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
        PC = pcA;
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
        //return estado; 
    }
    
}
