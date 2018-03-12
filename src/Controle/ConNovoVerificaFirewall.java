/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controle;

import Modelo.ModArquiConfiguracao;
import Modelo.ModFilialCaixa;
import Modelo.ModHost;
import Visao.VerificaFireCaixa;
import Visao.VerificaFirewall;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

/**
 *
 * @author programador
 */
public class ConNovoVerificaFirewall extends Thread {
    
    ModFilialCaixa modfi;
    ModArquiConfiguracao modArq;
    ConUtils conUtis;
    
    String user = "";
    String passwd = "";
    String sever  = "";
    String directory = "";
    
    //LINUX
    ModHost host;
    String path = "/ecf2000_solicita/";
    String config = ""+path+"config/host.cfg";
    String transmite = ""+path+"transmite/";
    String caminhoArquivo = "/ecf2000_solicita/config/host.cfg";
     
    //Windows
    /*ModHost host;
    String path = "\\ecf2000_solicita\\";
    String config = "" + path + "\\config\\host.cfg";
    String transmite = "" + path + "transmite\\";
    */
    ArrayList<ModFilialCaixa> fiCa;
    String rede;
    VerificaFireCaixa arqConfig;
    StyledDocument styledDocument;

    public ConNovoVerificaFirewall(VerificaFireCaixa veriFireCaixa, ArrayList<ModFilialCaixa> dadosFire, StyledDocument styledDocument) {
        this.arqConfig = veriFireCaixa;
        this.fiCa = dadosFire;
        this.styledDocument = styledDocument;
        conUtis = new ConUtils();
    }
    
    public void run () {
        ArrayList<String> arrayConfi = new ArrayList<String>();
        for (int i = 0; i < fiCa.size(); i++) {
            String ip = fiCa.get(i).getIp();
            
            boolean conect = true;

            if (conect) {
                try {
                    arqConfig.setjTFirewall("Conectado IP: " + ip, conUtis.estilosText(styledDocument, 6));
                    
                    FTPClient f = new FTPClient();
                    f.connect(ip);
                    f.login(this.user, this.passwd);
                    FTPFile[] files = f.listFiles(directory);
                    System.out.println(files.length);
                    if (files.length > 40) {
                        arqConfig.setjTFirewall(" | DBFire parado no Firewall |" +ip+" Quantidade| "+files.length+"\n", conUtis.estilosText(styledDocument, 7));
                        arrayConfi.add("| DBFire parado no Firewall |" +ip+" Quantidade| "+files.length+"\n");
                    } else {
                        arqConfig.setjTFirewall("\n", conUtis.estilosText(styledDocument, 0));
                    }
                    f.disconnect();
                   
                } catch (Exception e) {
                    try {
                        arrayConfi.add(" | Falha Conexao |" +ip +"\n");
                        arqConfig.setjTFirewall(" | Falha de conexao | " +ip +"\n", conUtis.estilosText(styledDocument, 2));
                        e.printStackTrace();
                    } catch (BadLocationException ex) {
                        Logger.getLogger(ConNovoVerificaFirewall.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
         
            }
   
        }
        try {
            MontaArqConfiguracao(arrayConfi);
        } catch (IOException ex) {
            Logger.getLogger(ConVerificaFirewall.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void MontaArqConfiguracao(ArrayList<String> modArq) throws IOException {
               
        Path path = Paths.get("/ecf2000_solicita/config/log_firewall.txt");
        Charset utf8 = StandardCharsets.UTF_8;
       
        try {
            arqConfig.setjTFirewall("Criando o arquivo de configuração.\nAguarde...\n", conUtis.estilosText(styledDocument, 0));
        } catch (BadLocationException ex) {
            Logger.getLogger(ConNovoVerificaFirewall.class.getName()).log(Level.SEVERE, null, ex);
        }
        //Verifica se o arquivo host.cfg existe.
        if (!Files.isReadable(path)) { 
            /*
            Caso o arquivo não exista, ele sera criado.
            */
            try {
                Files.createFile(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } 
        
        try (BufferedWriter whiter = Files.newBufferedWriter(path, utf8)) {
            for (int i = 0; i < modArq.size(); i++) {
                whiter.write(modArq.get(i));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        try {
            arqConfig.setjTFirewall("Fim do programa.\nArquivo criado com sucesso!!!\n", conUtis.estilosText(styledDocument, 0));
        } catch (BadLocationException ex) {
            Logger.getLogger(ConNovoVerificaFirewall.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
