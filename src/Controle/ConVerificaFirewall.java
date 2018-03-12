/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controle;

import Modelo.ModArquiConfiguracao;
import Modelo.ModFilialCaixa;
import Modelo.ModHost;
import Visao.VerificaFirewall;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

/**
 *
 * @author jaguar
 */
public class ConVerificaFirewall extends Thread {

    ModFilialCaixa modfi;
    ModArquiConfiguracao modArq;
    
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
    VerificaFirewall arqConfig;
    public ConVerificaFirewall() {
        modfi = new ModFilialCaixa();
        modArq = new ModArquiConfiguracao();
        fiCa = new ArrayList<ModFilialCaixa>();
        rede = "";
        //arqConfig = new ArquivoConfiguracao();
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPasswd() {
        return passwd;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }
    
    public String getSever() {
        return sever;
    }

    public void setSever(String sever) {
        this.sever = sever;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }
    
    
    
    public ArrayList<ModFilialCaixa> criaModeloFilialCaixa(int filial, int caixa, boolean local) {
        ArrayList<ModFilialCaixa> filialCaixa = new ArrayList<ModFilialCaixa>();
        int n = 1;
        int filialAux;
        
        if (local)
            filialAux = 0;
        else 
            filialAux = 1;

        for (; filialAux <= filial; filialAux++) {
            modfi = new ModFilialCaixa();
            modfi.setFilial(String.valueOf(filialAux));
            modfi.setCaixa(String.valueOf(caixa));
            filialCaixa.add(modfi);
            
        }
        return filialCaixa;
    }
    
    public void listConeccao(ArrayList<ModFilialCaixa> fiCa, String rede, VerificaFirewall veriCaixas) {
        this.fiCa = fiCa;
        this.rede = rede;
        this.arqConfig = veriCaixas;
    }
    
    public void run () {
        ArrayList<String> arrayConfi = new ArrayList<String>();
        for (int i = 0; i < fiCa.size(); i++) {
            String ip = rede + "." + fiCa.get(i).getFilial() + "." + fiCa.get(i).getCaixa();
            
            //arqConfig.setArea_configuracao("Pingando IP: "+ ip +"\n");
            
            boolean conect = true;

            if (conect) {
                try {
                    arqConfig.setArea_configuracao("Conectado IP: " + ip);
                    
                    FTPClient f = new FTPClient();
                    f.connect(ip);
                    f.login(this.user, this.passwd);
                    FTPFile[] files = f.listFiles(directory);
                    System.out.println(files.length);
                    if (files.length > 40) {
                        arqConfig.setArea_configuracao(" | DBFire parado no Firewall |" +ip+" Quantidade| "+files.length+"\n");
                        arrayConfi.add("| DBFire parado no Firewall |" +ip+" Quantidade| "+files.length+"\n");
                    } else {
                        arqConfig.setArea_configuracao("\n");
                    }
                    f.disconnect();
                   
                } catch (Exception e) {
                    arrayConfi.add(" | Falha Conexao |" +ip +"\n");
                    arqConfig.setArea_configuracao(" | Falha de conexao | " +ip +"\n");
                    e.printStackTrace();
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
       
        arqConfig.setArea_configuracao("Criando o arquivo de configuração.\nAguarde...\n");
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
        
        arqConfig.setArea_configuracao("Fim do programa.\nArquivo criado com sucesso!!!\n");
    }
}
