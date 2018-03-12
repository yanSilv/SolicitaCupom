/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controle;

import Modelo.ModArquiConfiguracao;
import Modelo.ModFilialCaixa;
import Modelo.ModHost;
import Visao.VerificaCaixas;
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

/**
 *
 * @author jaguar
 */
public class ConVerificaCaixas extends Thread {

    ModFilialCaixa modfi;
    ModArquiConfiguracao modArq;
    
    String user = "";
    String passwd = "";
    String endString = ":~#";
    
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
    VerificaCaixas arqConfig;
    public ConVerificaCaixas() {
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
    
    
    public ArrayList<ModFilialCaixa> criaModeloFilialCaixa(int filial, int caixa, boolean local) {
        ArrayList<ModFilialCaixa> filialCaixa = new ArrayList<ModFilialCaixa>();
        int n = 1;
        int filialAux;
        int caixaAux = 1;
        
        if (local)
            filialAux = 0;
        else 
            filialAux = 1;

        for (; filialAux <= filial; filialAux++) {
            for (caixaAux = 1; caixaAux <= caixa; caixaAux++) {
                modfi = new ModFilialCaixa();
                modfi.setFilial(String.valueOf(filialAux));
                modfi.setCaixa(String.valueOf(caixaAux));
                filialCaixa.add(modfi);
            }
        }
        return filialCaixa;
    }
    
    public void listConeccao(ArrayList<ModFilialCaixa> fiCa, String rede, VerificaCaixas veriCaixas) {
        this.fiCa = fiCa;
        this.rede = rede;
        this.arqConfig = veriCaixas;
    }
    
    public void run () {
        ArrayList<String> arrayConfi = new ArrayList<String>();
        //System.out.println(fiCa.size());
        for (int i = 0; i < fiCa.size(); i++) {
            String ip = rede + "." + fiCa.get(i).getFilial() + "." + fiCa.get(i).getCaixa();
            
            arqConfig.setArea_configuracao("Pingando IP: "+ ip +"\n");
            
            boolean conect = false;
            try {
                if (InetAddress.getByName(ip).isReachable(10000)) {
                    conect = true;
                } else {
                    conect = false;
                    arqConfig.setArea_configuracao("Sem comunicacao IP: "+ ip +"\n");
                }
            } catch (Exception e) {
                System.out.println("Ping falhou...");
            }

            if (conect) {
                try {
                    arqConfig.setArea_configuracao("Conectando IP: " + ip + "\n");
                    
                    JSch jsch = new JSch();
                    Session session = jsch.getSession(this.user, ip, 22);
                    session.setPassword(this.passwd);
                    session.setConfig("StrictHostKeyChecking", "no");
                    session.connect();

                    Channel channel = session.openChannel("shell");
                    channel.connect();

                    DataInputStream dataIn = new DataInputStream(channel.getInputStream());
                    DataOutputStream dataOut = new DataOutputStream(channel.getOutputStream());

                    dataOut.writeBytes("killall trarquivo.tcl\r\n");
                    dataOut.flush();
                    
                    String line = dataIn.readLine();
                    
                    while (!line.endsWith(this.endString) || !line.endsWith("killall")) {
                        String linha[] = line.split("-");
                        System.out.println(line);
                        if (linha.length > 6) {
                          
                            modArq = new ModArquiConfiguracao();
                            for (int j = 0; j < linha.length; j++) {
                                switch (j) {
                                    case 0:
                                        modArq.setNumFilial(linha[j]);
                                        break;
                                    case 1:
                                        modArq.setNumCaixa(linha[j]);
                                        break;
                                    case 5:
                                        modArq.setNumIp(linha[j]);
                                        break;
                                    case 7:
                                        modArq.setNumImpressora(linha[j]);
                                        break;
                                }
                            }
                        }
                        
                        if (line.length() > 0 && line.length() < 7) {
                       
                            if (Integer.parseInt(line) > 40) {
                              
                                String filial = StringUtils.leftPad(fiCa.get(i).getFilial(), 3, '0');
                                String caixa  = StringUtils.leftPad(fiCa.get(i).getCaixa(), 3, '0');
                                String quant  = StringUtils.leftPad(line, 7,'0');
                                String log = "FILIAL| "+filial+"| Caixa| "+caixa+"| IP| "+ip+" | Impressora | "+modArq.getNumImpressora()+"|Quantidade |"+quant+"| ARQUIVOS PARADO NO TRANSMITE\n\r";
                                arqConfig.setArea_configuracao(log);
                                arrayConfi.add(log);
                            }
                            break;
                        }
                    
                        line = dataIn.readLine();
                    }
                    
                    dataIn.close();
                    dataOut.close();
                    channel.disconnect();
                    session.disconnect();
                } catch (Exception e) {
                    System.out.println("ERRO - try - catch Verificação!!!");
                    e.printStackTrace();
                }
         
            }
   
        }
        try {
            MontaArqConfiguracao(arrayConfi);
        } catch (IOException ex) {
            Logger.getLogger(ConVerificaCaixas.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void MontaArqConfiguracao(ArrayList<String> modArq) throws IOException {
               
        Path path = Paths.get("/ecf2000_solicita/config/log.txt");
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
