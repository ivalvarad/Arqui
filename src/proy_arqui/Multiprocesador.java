package proy_arqui;


import java.util.ArrayList;
import proy_arqui.CargadorArchivos;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Iva
 * Esta clase se encarga de controlar todo el multiprocesador, es el hilo principal
 * Aqui van a estar declaradas todas las estructuras que representan la memoria, cache y demas de cada procesador
 * Aqui inicia el programa
 */
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
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        Multiprocesador mp = new Multiprocesador();
        Simulacion sim = new Simulacion();
        Procesador proc = new Procesador(mp);
        CargadorArchivos crg = new CargadorArchivos(mp, sim);
        crg.setVisible(true);
    }
 
}
