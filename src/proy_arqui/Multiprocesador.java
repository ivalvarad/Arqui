package proy_arqui;

import java.util.ArrayList;
import proy_arqui.CargadorArchivos;

public class Multiprocesador {

    private ArrayList instrucciones = new ArrayList();
    private ArrayList pcs = new ArrayList();
    private int numHilitos;
    
    public void agregarInstruccion(int num){
        instrucciones.add(num);
    }
    
    public void verInstrucciones(){
        System.out.println("Se han cargado "+numHilitos+" programas.");
        System.out.println("El arreglo de instrucciones hasta el momento es el siguiente:");
        for(int i=0; i < instrucciones.size(); i++){
            System.out.print(instrucciones.get(i)+ " ");
        }
        System.out.println();
        System.out.println("Los indices donde inicia cada programa(PCs) son los siguientes:");
        for(int i=0; i < pcs.size(); i++){
            System.out.print(pcs.get(i)+ " ");
        }
        System.out.println();
    }
    
    public void sumarHilito(){
        numHilitos++;
    }
    
    public void agregarPc(){
        pcs.add(instrucciones.size());
    }

    public static void main(String[] args) {
        // TODO code application logic here
        Multiprocesador mp = new Multiprocesador();
        Simulacion sim = new Simulacion();
        Procesador proc = new Procesador(mp);
        CargadorArchivos crg = new CargadorArchivos(mp, sim);
        crg.setVisible(true);
    }
}
