/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Controle;

import Modelo.ModFilialCaixa;
import java.awt.Color;
import java.util.ArrayList;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

/**
 *
 * @author jaguar
 */
public class ConUtils {

    public ArrayList<ModFilialCaixa> dadosRede(ModFilialCaixa mod, int i) {
        int MIN = Integer.parseInt(mod.getFilial());
        int MAX = Integer.parseInt(mod.getFilialFinal());
        int num = Integer.parseInt(mod.getCaixa());
        
        ArrayList<ModFilialCaixa> retorno = new ArrayList<ModFilialCaixa>();
        
        String ip;
        
        for(int j = MIN; j <= MAX; j ++){
            /*
            i = 0 monta ip dos caixa
            i = 1 monta ip dos firewall
            */
            if (i == 0) {
                for ( int k = 1; k <= num; k ++) {
                    ModFilialCaixa modelo = new ModFilialCaixa();
                    modelo.setFilial(mod.getFilial());
                    
                    ip = "";
                    ip = ip.concat(mod.getIp());
                    ip = ip.concat(".");
                    ip = ip.concat(String.valueOf(j));
                    ip = ip.concat(".");
                    ip = ip.concat(String.valueOf(k));
                   
                    modelo.setIp(ip);
                    retorno.add(modelo);
                }
            } else if (i == 1) {
                ModFilialCaixa modelo = new ModFilialCaixa();
              
                modelo.setFilial(String.valueOf(j));
                
                ip = "";
                ip = ip.concat(mod.getIp());
                ip = ip.concat(".");
                ip = ip.concat(String.valueOf(j));
                ip = ip.concat(".");
                ip = ip.concat(String.valueOf(num));

                modelo.setIp(ip);
                retorno.add(modelo);
            }
        }
        
        return retorno;
    }


    public Style estilosText(StyledDocument styledDocument, int opcao) {
        StyledDocument doc = styledDocument;
        
        Style def = StyleContext.getDefaultStyleContext().getStyle( StyleContext.DEFAULT_STYLE );
        Style regular = doc.addStyle( "regular", def );
        
        // Create an italic style
        Style italic = doc.addStyle( "italic", regular );
        StyleConstants.setItalic( italic, true );
        
        // Create a bold style
        Style bold = doc.addStyle( "bold", regular );
        StyleConstants.setBold( bold, true );

        // Create a small style
        Style small = doc.addStyle( "small", regular );
        StyleConstants.setFontSize( small, 10 );

        // Create a large style
        Style large = doc.addStyle( "large", regular );
        StyleConstants.setFontSize( large, 16 );

        // Create a superscript style
        Style superscript = doc.addStyle( "superscript", regular );
        StyleConstants.setSuperscript( superscript, true );

        // Create a highlight style
        Style highlight = doc.addStyle( "highlight", regular );
        StyleConstants.setBackground( highlight, Color.yellow );
        
        Style red = doc.addStyle( "red", regular );
        StyleConstants.setBackground( red, Color.red );
        
        switch (opcao) {
            case 0: return regular;
            case 1: return italic;
            case 2: return bold;
            case 3: return small;
            case 4: return large;
            case 5: return superscript;
            case 6: return highlight;
            case 7: return red;
            default: return regular;
        } 
    }
}
