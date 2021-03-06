/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package dominio;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Daniel Prett, Gabriel Saldanha, Igor Martire, Lucas Barros
 */
public class Tabela {
    
    private String nome;
    private String chave;
    private List<Atributo> atributos;

    public Tabela(String nome, String chave) throws IllegalArgumentException{
        this(nome, chave, new ArrayList<Atributo>());
    }

    public Tabela(String nome, String chave, List<Atributo> atributos) throws IllegalArgumentException{
        this.setNome(nome);
        this.setChave(chave);
        this.setAtributos(atributos);
    }
    
    public void addAtributo(Atributo a) {
        if(a != null)
            atributos.add(a);
    }

    public String getNome() {
        return nome;
    }

    public String getNomeChave() {
        return chave;
    }

    public List<Atributo> getAtributos() {
        return atributos;
    }

    private void setNome(String nome) throws IllegalArgumentException{
        if(nome == null || nome.equals("")){
            throw new IllegalArgumentException("[ERRO] Valor de atributo:\n"
                    + this.getClass().toString() + ".nome nao pode"
                    + " receber um valor nulo ou vazio (\"\").");
        }
        this.nome = nome;
    }

    private void setChave(String chave) throws IllegalArgumentException{
        if(chave == null || chave.equals("")){
            throw new IllegalArgumentException("[ERRO] Valor de atributo:\n"
                    + this.getClass().toString() + ".chave nao pode"
                    + " receber um valor nulo ou vazio (\"\").");
        }
        this.chave = chave;
    }

    private void setAtributos(List<Atributo> atributos) throws IllegalArgumentException{
        if(atributos == null){
            throw new IllegalArgumentException("[ERRO] Valor de atributo:\n"
                    + this.getClass().toString() + ".atributos nao pode"
                    + " receber um valor nulo");
        }
        this.atributos = atributos;
    }

    public void salva(DataOutputStream out) throws IOException {
        out.writeUTF(nome);
        out.writeUTF(chave);
        for (Atributo a : atributos) {
            a.salva(out);
        }
        //Salva marcador de fim da tabela
        Atributo marcadorFim = new Atributo("x",Atributo.TIPO_MARCADOR);
        marcadorFim.salva(out);
    }
    
    public static Tabela le(DataInputStream in) throws IOException {
        String nomeTabela = in.readUTF();        
        String nomeAtributoChave = in.readUTF();
        Tabela t = new Tabela(nomeTabela,nomeAtributoChave);        
        boolean fim = false;
        do{
            Atributo a = Atributo.le(in);
            if(a.getTipo() == Atributo.TIPO_MARCADOR){
                fim = true;
            }
            else{
                t.addAtributo(a);
            }            
        }while(!fim);
        return t;
    }
    
    @Override
    public String toString(){
        String str = "Nome da tabela: "+this.nome;
        str+="\nAtributo chave: "+this.chave;
        for(Atributo a : this.atributos) {
            str+="\nAtributo (nome : tipo): "+a;
        }        
        return str;
    }
    
    // Retorna um atributo (que não a chave) pelo nome dele
    public Atributo getAtributoByName(String nomeAtributo) {
        for(Atributo a : atributos) {
            if(a.getNome().equalsIgnoreCase(nomeAtributo)) {
                return a;
            }
        }
        return null;
    }
    
    //Retorna o tamanho de um registro dessa tabela
    public int getTamanhoRegistro() {
        int tamanhoRegistro = 4; // 4 bytes devido à chave
        for (Atributo a : atributos) {
            if (a.getTipo() == Atributo.TIPO_INTEIRO) {
                tamanhoRegistro += 4;
            }
            else {
                tamanhoRegistro += Valor.TAMANHO_LIMITE_TEXTO + 2;
            }
        }
        tamanhoRegistro += 4 + 1; //prox(integer) e flag(boolean)
        return tamanhoRegistro;
    }
    
    /**
     * Retorna o nome do atributo a partir do seu índice
     * @param index index -1 se refere ao atributo-chave; index >=0 se refere ao atributo de index correspondente na lista atributos
     * @return Nome do atributo de index passado como parâmetro
     */
    public String getNomeAtributoByIndex(int index) {
        String nomeAtr;
        if (index == -1) {
            nomeAtr = this.getNomeChave();
        }
        else {
            nomeAtr = this.atributos.get(index).getNome();
        }
        return nomeAtr;
    }
    
    public String toString(List<Integer> indexAtributosSelecionados) {        
        String str = "|";
        for (Integer index : indexAtributosSelecionados) {
            str += " ";
            String nomeAtr = getNomeAtributoByIndex(index);            
            while (nomeAtr.length() < Valor.TAMANHO_LIMITE_TEXTO) {
                nomeAtr += " ";
            }
            str += nomeAtr;            
            str += " |";
        }
        return str;
    }
    
    /**
     * Retorna uma string para separar tuplas da tabela a partir do número de atributos que estão sendo mostrados na tabela
     * @param n número de atributos que estão sendo mostrados na tabela
     * @return string utilizada para separar tuplas da tabela
     */
    public static String getLineSeparator(int n) {        
        String ls = "";
        int aux = Valor.TAMANHO_LIMITE_TEXTO + 3;
        for (int i = 0; i < (aux*n + 1) ; i++) {
            if(i % aux == 0)
                ls += "+";
            else
                ls += "-";
        }
        return ls;
    }
}
