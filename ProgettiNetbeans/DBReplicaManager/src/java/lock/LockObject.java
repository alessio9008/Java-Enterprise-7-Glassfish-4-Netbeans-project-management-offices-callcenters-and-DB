/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lock;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Alessio_Gregory_Ricky
 */
public class LockObject {

    private boolean blocked;
/**
 * The Lock Object is a way to implement the locking system.
 */
    public LockObject() {
        blocked = false;
    }
/**
 * Try to lock the resource without any listen phase. Immediately returns a response, after the check.
 * @return 
 */
    public synchronized boolean tryLock() {
        if (blocked) {
            return false;
        } else {
            blocked = true;
            return true;
        }
    }
/**
 * Unlock the resource previously locked.
 */
    public synchronized void unLock() {
        blocked = false;
    }

//    public synchronized void Lock(){
//        if(blocked){
//            try {
//                this.wait();
//            } catch (InterruptedException ex) {
//                ex.printStackTrace();
//            }
//            blocked=true;
//        }
//    }
//    
//     public synchronized void unLock(){
//        blocked=false;
//        this.notifyAll();
//    }
}
