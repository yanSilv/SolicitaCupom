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
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
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
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author jaguar
 */
public class conNovoVerificaCaixa extends Thread {
    
    VerificaFireCaixa arqConfig;
    ArrayList<ModFilialCaixa> fiCa;
    ConUtils conUtis;
    
    ModArquiConfiguracao modArq;
    StyledDocument styledDocument;
     
    String user = "root";
    String passwd = "123456";
    String endString = "trado";
    
    //LINUX
    ModHost host;
    String path = "/ecf2000_solicita/";
    String config = ""+path+"config/host.cfg";
    String transmite = ""+path+"transmite/";
    String caminhoArquivo = "/ecf2000_solicita/config/host.cfg";

    public conNovoVerificaCaixa(VerificaFireCaixa veriCaixa, ArrayList<ModFilialCaixa> dadosCaixa, StyledDocument styledDocument) {
        this.arqConfig = veriCaixa;
        this.fiCa = dadosCaixa;
        modArq = new ModArquiConfiguracao();
        conUtis = new ConUtils();
        this.styledDocument = styledDocument;
    }
    
     public void run () {
        ArrayList<String> arrayConfi = new ArrayList<String>();
        System.out.println(fiCa.size());
        for (int i = 0; i < fiCa.size(); i++) {
            String ip = fiCa.get(i).getIp();
            
            System.out.println(fiCa.get(i).getFilial());
            try {
                arqConfig.setjTCaixas("Pingando IP: "+ ip +" |", conUtis.estilosText(this.styledDocument, 6));
            } catch (BadLocationException ex) {
                Logger.getLogger(conNovoVerificaCaixa.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            boolean conect = false;
            try {
                if (InetAddress.getByName(ip).isReachable(10000)) {
                    conect = true;
                } else {
                    conect = false;
                    arqConfig.setjTCaixas(" Sem comunicacao IP: "+ ip +"\n", conUtis.estilosText(this.styledDocument, 7));
                }
            } catch (Exception e) {
                System.out.println("Ping falhou...");
            }

            if (conect) {
                try {
                    arqConfig.setjTCaixas("Conectando IP: " + ip + "\n", conUtis.estilosText(this.styledDocument, 1));
                    
                    JSch jsch = new JSch();
                    Session session = jsch.getSession(this.user, ip, 22);
                    session.setPassword(this.passwd);
                    session.setConfig("StrictHostKeyChecking", "no");
                    session.connect();

                    Channel channel = session.openChannel("shell");
                    channel.connect();

                    DataInputStream dataIn = new DataInputStream(channel.getInputStream());
                    DataOutputStream dataOut = new DataOutputStream(channel.getOutputStream());

                    dataOut.writeBytes("cat /ecf2000/recebe/noar.txt;ls /ecf2000/transmite/|wc -l\r\n");
                    dataOut.flush();

                    String line = dataIn.readLine();
                    
                    while (!line.endsWith(this.endString) || !line.endsWith("ectory")) {
                        String linha[] = line.split("-");
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
                        
                        if (line.length() > 0 &&line.length() < 7) {
                       
                            if (Integer.parseInt(line) > 40) {
                              
                                String filial = StringUtils.leftPad(fiCa.get(i).getFilial(), 3, '0');
                                String caixa  = StringUtils.leftPad(fiCa.get(i).getCaixa(), 3, '0');
                                String quant  = StringUtils.leftPad(line, 7,'0');
                                String log = "FILIAL| "+filial+"| Caixa| "+caixa+"| IP| "+ip+" | Impressora | "+modArq.getNumImpressora()+"|Quantidade |"+quant+"| ARQUIVOS PARADO NO TRANSMITE\n\r";
                                arqConfig.setjTCaixas(log, conUtis.estilosText(this.styledDocument, 1));
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
            try {
                MontaArqConfiguracao(arrayConfi);
            } catch (BadLocationException ex) {
                Logger.getLogger(conNovoVerificaCaixa.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (IOException ex) {
            Logger.getLogger(ConVerificaCaixas.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
     
     private void MontaArqConfiguracao(ArrayList<String> modArq) throws IOException, BadLocationException {
               
        Path path = Paths.get("/ecf2000_solicita/config/log.txt");
        Charset utf8 = StandardCharsets.UTF_8;
       
        arqConfig.setjTCaixas("Criando o arquivo de configuração.\nAguarde...\n", conUtis.estilosText(this.styledDocument, 1));
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
        
        arqConfig.setjTCaixas("Fim do programa.\nArquivo criado com sucesso!!!\n", conUtis.estilosText(this.styledDocument, 1));
    }
    
}
