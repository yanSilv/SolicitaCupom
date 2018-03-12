/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Controle;

import com.jcraft.jsch.UserInfo;

/**
 *
 * @author jaguar
 */
public class MPInfoUser implements UserInfo {

    @Override
    public String getPassphrase() {
        return null;
    }

    @Override
    public String getPassword() {
        return "123456";
    }

    @Override
    public boolean promptPassword(String string) {
        return true;
    }

    @Override
    public boolean promptPassphrase(String string) {
        return true;
    }

    @Override
    public boolean promptYesNo(String string) {
        return true;
    }

    @Override
    public void showMessage(String string) {
        
    }
    
    
}
