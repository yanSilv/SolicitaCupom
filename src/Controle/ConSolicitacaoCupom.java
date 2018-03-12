/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Controle;
import Modelo.ModHost;
import Visao.Solicita;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author jaguar
 */
public class ConSolicitacaoCupom {
    
    ModHost host;
    String path = "/ecf2000_solicita/";
    String config = ""+path+"config/host.cfg";
    String transmite = ""+path+"transmite/"; 
    
    public ConSolicitacaoCupom () {
        host = new ModHost();
    }

    public ArrayList<ModHost> lerHost(){
        ArrayList<ModHost> reg = new ArrayList<ModHost>();
        try {
            BufferedReader buffeRead = new BufferedReader(new FileReader(this.config));
            String linha = "";
            while(true) {
                if (linha != null) {
                    String[] arrayDados = linha.split(";");
                    ModHost registro = new ModHost();
                    
                    for (int i = 0; i < arrayDados.length; i++) {
                        switch (i) {
                            case 0: registro.setFilial(arrayDados[i]);
                                break;
                            case 1: registro.setCaixa(arrayDados[i]);
                                break;
                            case 2: registro.setEndereco(arrayDados[i]);
                                break;   
                        }
                    }
                    reg.add(registro);
                } else {
                    break;
                }
                linha = buffeRead.readLine();

            }
            buffeRead.close();
        } catch (IOException ex) {
                Logger.getLogger(ConSolicitacaoCupom.class.getName()).log(Level.SEVERE, null, ex);
        }
        return reg;
    }

    public boolean montaSolicitacao(ArrayList<String> dados) throws IOException {
        
        int tipoSolicitacao = Integer.parseInt(dados.get(0));
        
        if (tipoSolicitacao == 0) {
         return leituraMFD(dados);
        } else if(tipoSolicitacao == 1) {
            bancoDados(dados);
        }
        return true;
    }

    private boolean leituraMFD(ArrayList<String> dados) throws IOException {
        String filial = StringUtils.leftPad(dados.get(1), 3, '0');
        String caixa  = StringUtils.leftPad(dados.get(2), 2, '0');
        String cupomIni = StringUtils.leftPad(dados.get(4), 6, '0');
        String cupomFim = StringUtils.leftPad(dados.get(5), 6, '0');
        
        String arquivo = "leituraMFDcoo.txt";
        String linha = "";
        
        File file = new File(this.transmite+""+arquivo);
        
        if (file.isFile()){
            file.delete();
        }
        
        linha = filial +";"+ caixa +";"+ cupomIni +";"+cupomFim +"\n";
        
        boolean sucesso = file.createNewFile();
        
        if (sucesso) {
            BufferedWriter buffWriter = new BufferedWriter(new FileWriter(file));
            buffWriter.append(linha);
            buffWriter.close();
        } else {
            return false;
        }
        return true;
    }

    private boolean bancoDados(ArrayList<String> dados) throws IOException {
        String filial = StringUtils.leftPad(dados.get(1), 3, '0');
        String caixa  = StringUtils.leftPad(dados.get(2), 2, '0');
        String data   = dados.get(3);
        String cupomIni = StringUtils.leftPad(dados.get(4), 6, '0');
        String cupomFim = StringUtils.leftPad(dados.get(5), 6, '0');
        
        String arquivo = "cupomdb.txt";
        String linha = "";
        
        File file = new File(this.transmite+""+arquivo);
        
        if (file.isFile()){
            file.delete();
        }
        
        linha = dataseq(data) +";"+ filial +";"+ caixa +";"+ cupomIni +";"+cupomFim +"\n";
        
        boolean sucesso = file.createNewFile();
        
        if (sucesso) {
            BufferedWriter buffWriter = new BufferedWriter(new FileWriter(file));
            buffWriter.append(linha);
            buffWriter.close();
        } else {
            return false;
        }
        return true;
        
    }

    public boolean enviaSolicitacao(ArrayList<String> dados, ArrayList<ModHost> listHost, Solicita soli) {
        int tipoSolicitacao = Integer.parseInt(dados.get(0));
        String filial = StringUtils.leftPad(dados.get(1), 3, '0');
        String caixa  = StringUtils.leftPad(dados.get(2), 2, '0');
       
        String host = "";
        String usuario = "root";
        String senha   = "123456";
        
        String file = "";
        String caminho = "";
        
        if (tipoSolicitacao == 0) {
            file = "leituraMFDcoo.txt"; 
        } else if(tipoSolicitacao == 1) {
            file = "cupomdb.txt";
        }
        
        caminho = "" +this.transmite+file;
        
        for(int i = 0; i < listHost.size(); i++) {
            if (filial.equals(listHost.get(i).getFilial()) && caixa.equals(listHost.get(i).getCaixa())) {
                host = listHost.get(i).getEndereco();
            }
        }
        
        FTPClient ftp = new FTPClient();
        
        try{
            soli.setJlResposta("Conectando...");
            ftp.connect(host);
            soli.setJlResposta("Conectou...");
            if(FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
                soli.setJlResposta("Autenticando...");
                ftp.login(usuario, senha);
                soli.setJlResposta("Autenticou...");
                soli.setJlResposta("Enviando arquivo "+ file +" para " + host);
                InputStream is = new FileInputStream(caminho);
                ftp.storeFile("/ecf2000/recebe/"+file, is);
                soli.setJlResposta("Arquivo enviado");
            } else {
                //erro ao se conectar
                ftp.disconnect();
                System.out.println("Conexão recusada");
                System.exit(0);
            }
            
        } catch(SocketException ex){
            soli.setJlResposta("Falha ao se conectar:Verifique o FTPD");
        } catch (IOException ex) {
            soli.setJlResposta("Falha ao se conectar:Veritique o diretorio do arquivo");
        }
        
        return true;
    }

    private String dataseq(String data) {
        String arrayData[] = data.split("/");
        String dataInvert  = arrayData[2] + arrayData[1] + arrayData[0];
        
        return dataInvert;
    }
}
