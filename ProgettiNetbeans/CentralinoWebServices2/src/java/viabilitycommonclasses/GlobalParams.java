/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package viabilitycommonclasses;

import java.io.Serializable;

/**
 *Parametri globali sistema.
 * @author Alessio_Gregory_Ricky
 */
public class GlobalParams implements Serializable{
    /**
     * Numero Uffici per ogni TM
     */
    public static int numOffice=5;
    /**
     * Numero massimo di attivita' in coda
     */
    public static int numMaxQueueActivity=15;
}
