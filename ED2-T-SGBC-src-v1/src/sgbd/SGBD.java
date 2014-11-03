package sgbd;

import java.util.Scanner;
import dominio.*;
import hash.EncadeamentoInterior;
import hash.Result;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.LinkedList;
import java.util.List;

/**
* @author Daniel Prett, Gabriel Saldanha, Igor Martire, Lucas Barros
*/
public class SGBD {

    private static final int TAMANHO_TABELA_HASH = 23;
    private static final int OP_CRIAR_TABELA = 1;
    private static final int OP_MOSTRAR_TABELAS = 2;
    private static final int OP_INSERIR_REGISTRO = 3;
    private static final int OP_CONSULTAR_REGISTROS = 4;
    private static final int OP_EXCLUIR_REGISTROS = 5;
    private static final int OP_MODIFICAR_REGISTRO = 6;
    private static final int OP_SAIR = 7;
    private static final String ARQUIVO_CATALOGO = "catalogo.dat";       
    private static final EncadeamentoInterior HASH_MASTER = new EncadeamentoInterior(TAMANHO_TABELA_HASH);
    static final Scanner SCAN = new Scanner(System.in);   
    
    /**
    * Interface com o usuário
    */
    public static void main(String[] args) {
        System.out.println  ("#####################\n"
                            +"# ED2 - SGBD        #\n"
                            +"# Grupo:            #\n"
                            +"# *Daniel Campagna  #\n"
                            +"# *Gabriel Saldanha #\n"
                            +"# *Igor Martire     #\n"
                            +"# *Lucas Barros     #\n"
                            +"#####################\n");        
        boolean sair = false;
        while (!sair) {
            System.out.println  ("Entre com o codigo da opcao desejada:\n"
                                +OP_CRIAR_TABELA+"- Criar tabela\n"
                                +OP_MOSTRAR_TABELAS+"- Mostrar tabelas\n"
                                +OP_INSERIR_REGISTRO+"- Inserir registro\n"
                                +OP_CONSULTAR_REGISTROS+"- Consultar registros\n"
                                +OP_EXCLUIR_REGISTROS+"- Excluir registros\n"
                                +OP_MODIFICAR_REGISTRO+"- Modificar registro\n"
                                +OP_SAIR+"- Sair\n");
            
            System.out.print("Opcao: ");
            int op = SCAN.nextInt();  
            switch (op) {
                case OP_CRIAR_TABELA:
                    opCriarTabela();
                    break;
                case OP_MOSTRAR_TABELAS:
                    opMostrarTabelas();
                    break;
                case OP_INSERIR_REGISTRO:
                    opInserirRegistro();
                    break;
                case OP_CONSULTAR_REGISTROS:
                    opConsultarRegistros();
                    break;
                case OP_EXCLUIR_REGISTROS:
                    opExcluirRegistros();
                    break;
                case OP_MODIFICAR_REGISTRO:
                    opModificarRegistro();
                    break;
                case OP_SAIR:
                    sair = true;
                    break;
                default:
                    System.out.println("\nOpcao invalida.");
                break;
            }
            System.out.println("\n----------------------------\n");
        }
    }

    private static void opCriarTabela() {
        System.out.println("\n----------------------------\n");        
        
        //Nome e chave da tabela
        Tabela tabela = null;
        try {
            System.out.print("Entre com o nome da tabela: ");
            String nomeTabela = SCAN.next();
            
            //Se já existe tabela no banco de dados com o nome desejado, cancela a criação da nova tabela
            if(getTabelaByName(nomeTabela) != null) 
                throw new IllegalArgumentException("[Erro] Tabela com esse nome ja existe!");
                
            System.out.print("Entre com o nome do atributo chave (o tipo eh inteiro): ");
            String nomeChave = SCAN.next();
            
            tabela = new Tabela(nomeTabela,nomeChave);
        }
        catch (IllegalArgumentException ex){
            System.out.println(ex.getMessage());
        }
        
        if(tabela == null) {
            System.out.println("Criacao da tabela cancelada.");
            return;        
        }
        
        //Adição de atributos
        boolean fim;
        System.out.print("Deseja adicionar mais um atributo? (s/n): ");
        fim = !SCAN.next().startsWith("s");
        while(!fim) {            
            System.out.print("Entre com o nome do atributo: ");
            String nomeAtributo = SCAN.next();   
            
            //Se já foi adicionado um atributo com o mesmo nome, pede outro nome para o atributo.
            if((nomeAtributo.equalsIgnoreCase(tabela.getNomeChave())) || (tabela.getAtributoByName(nomeAtributo) != null)) {
                System.out.println("Já há um atributo com esse nome. Escolha outro nome.");
                continue;
            }
            
            try {
                Atributo a = null;
                boolean repete;
                do {
                    repete = false;
                    System.out.print("Entre com o tipo do atributo (1 para inteiro e 2 para texto): ");
                    int opTipoAtributo = SCAN.nextInt();                        
                    switch(opTipoAtributo){
                        case 1:
                            a = new Atributo(nomeAtributo,Atributo.TIPO_INTEIRO);
                            break;
                        case 2:
                            a = new Atributo(nomeAtributo,Atributo.TIPO_TEXTO);
                            break;
                        default:                    
                            System.out.println("Opcao invalida");
                            repete = true;
                            break;
                    }
                } while(repete);
                tabela.addAtributo(a);
            }
            catch (IllegalArgumentException ex){
                System.out.println(ex.getMessage());
            }            
                        
            System.out.print("Deseja adicionar mais um atributo? (s/n): ");
            fim = !SCAN.next().startsWith("s");           
        }
        
        //Gravação da tabela no arquivo de catálogo e criação do arquivo de registros
        DataOutputStream out = null;
        try {
            out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(ARQUIVO_CATALOGO,true)));
            tabela.salva(out); // salva tabela no arquivo de catálogo
            HASH_MASTER.criaHash(tabela); // cria arquivo de registros
            System.out.println("\nTabela criada com sucesso!");            
        } catch (IOException ex) {            
            System.out.println(ex.getMessage());            
            System.out.println("\nFalha na criacao da tabela!");
        } finally {
            if(out != null) {
                try {
                    out.close();
                } catch (IOException ex) {
                    System.out.println(ex.getMessage());
                }
            }
        }
        
    }

    private static void opMostrarTabelas() {
        System.out.println("\n----------------------------\n");  
        
        Tabela tabela;
        DataInputStream in = null;        
        try {
            in = new DataInputStream(new BufferedInputStream(new FileInputStream(ARQUIVO_CATALOGO)));
            while(true){
                tabela = Tabela.le(in);            
                System.out.println(tabela);
                System.out.println("********");
            }
        } catch (EOFException ex) {
            System.out.println("Todas as tabelas foram listadas.");
        } catch (FileNotFoundException ex) {
            System.out.println("Nao há tabelas no banco de dados.");
        } catch (IOException ex) {            
            System.out.println(ex.getMessage());            
        } finally {
            if(in != null) {
                try {
                    in.close();
                } catch (IOException ex) {
                    System.out.println(ex.getMessage());
                }
            }
        }
    }
    
    private static void opInserirRegistro() {
        System.out.println("\n----------------------------\n");        
       
        try {
            //Escolha da tabela
            System.out.print("Entre com o nome da tabela na qual deseja inserir um registro: ");
            String nomeTabela = SCAN.next();
            Tabela tabela = getTabelaByName(nomeTabela);
            //Se não existe tabela no banco de dados com o nome desejado, cancela a inserção do registro
            if(tabela == null) {
                throw new IllegalArgumentException("[Erro] Não existe tabela com esse nome.");
            }
            
            //Valor do atributo-chave
            System.out.print("Entre com o valor do atributo-chave "+tabela.getNomeChave()+" (o tipo eh inteiro): ");
            int valorChave = SCAN.nextInt();
                
            //Se valor da chave já existe na tabela, então cancela inserção
            Result res = HASH_MASTER.busca(valorChave, tabela);
            if (res.getA() == 1) {
                throw new IllegalArgumentException("[Erro] Já existe um registro com a mesma chave.");
            }
            
            //Valores dos demais atributos
            List<Valor> valoresAtributos = new ArrayList<Valor>();
            for(Atributo atr : tabela.getAtributos()) {
                Valor valor;
                switch(atr.getTipo()) {
                    case Atributo.TIPO_INTEIRO:
                        System.out.print("Entre com o valor do atributo "+atr.getNome()+" (o tipo eh inteiro): ");
                        int valorAtributoInteiro = SCAN.nextInt();
                        valor = new Valor(valorAtributoInteiro);
                        valoresAtributos.add(valor);
                        break;
                    case Atributo.TIPO_TEXTO:
                        System.out.print("Entre com o valor do atributo "+atr.getNome()+" (o tipo eh texto): ");
                        String valorAtributoTexto = SCAN.next();
                        valor = new Valor(valorAtributoTexto);
                        valoresAtributos.add(valor);
                        break;
                }                            
            }
                        
            int result = HASH_MASTER.insere(valorChave,valoresAtributos,tabela);
            switch (result) {
                case -1: //não deve aparecer porque o valor da chave já foi testado lá em cima
                    System.out.println("[ERRO] Nao foi possivel inserir o registro: "
                                     + "Ja existe um registro salvo com o mesmo valor de chave.");
                    System.out.println("Insercao de registro cancelada.");
                    break;
                case -2:
                    System.out.println("[ERRO] Nao foi possivel inserir o registro: "
                                     + "Nao ha mais espaco livre para inserir registros nessa tabela (overflow).");
                    System.out.println("Insercao de registro cancelada.");
                    break;
                default:
                    System.out.println("Registro inserido com sucesso.");
                    break;
            }            
        }
        catch (IllegalArgumentException ex){
            System.out.println(ex.getMessage());
            System.out.println("Insercao de registro cancelada.");
        }
        catch (InputMismatchException ex) {
            System.out.println("[ERRO] O valor entrado eh invalido.");
            System.out.println("Insercao de registro cancelada.");
        }
        catch(FileNotFoundException ex) {
            System.out.println("[ERRO] O arquivo de registros da tabela desejada nao foi encontrado.");
            System.out.println("Insercao de registro cancelada.");
        }
        catch(IOException ex) {
            System.out.println("[ERRO] Falha na leitura do arquivo de registros da tabela desejada.");
            System.out.println("Insercao de registro cancelada.");
        }
    }
    
    private static void opConsultarRegistros() {
        System.out.println("\n----------------------------\n");                
        
        try {
            //Escolha da tabela
            System.out.print("Entre com o nome da tabela da qual deseja consultar registros: ");
            String nomeTabela = SCAN.next();
            Tabela tabela = getTabelaByName(nomeTabela);
            //Se não existe tabela no banco de dados com o nome desejado, cancela a consulta de registros
            if(tabela == null) {
                throw new IllegalArgumentException("[Erro] Não existe tabela com esse nome.");
            }
             
            //Seleção dos atributos a serem consultados            
            List<Integer> indexAtributosSelecionados = new LinkedList<Integer>();
            int index = -1;
            System.out.print("Deseja consultar o atributo-chave "+tabela.getNomeChave()+"? (s/n): ");
            boolean consultar = SGBD.SCAN.next().startsWith("s");
            if(consultar){
                indexAtributosSelecionados.add(index);
            }
            for(Atributo atr : tabela.getAtributos()) {
                index++;
                System.out.print("Deseja consultar o atributo "+atr.getNome()+"? (s/n): ");
                consultar = SGBD.SCAN.next().startsWith("s");
                if(consultar){
                    indexAtributosSelecionados.add(index);
                }
            }            
            if(indexAtributosSelecionados.isEmpty()) {
                throw new IllegalArgumentException("[Erro] Pelo menos um atributo deve ser selecionado para consulta.");
            }
            
            //Criação dos filtros
            List<Filtro> filtros = Filtro.menuCriacaoFiltros(tabela);
            
            //Imprime resposta da consulta
            System.out.println("\nResultado da consulta:");
            System.out.println(Tabela.getLineSeparator(indexAtributosSelecionados.size()));
            System.out.println(tabela.toString(indexAtributosSelecionados));
            System.out.println(Tabela.getLineSeparator(indexAtributosSelecionados.size()));
            int contMostrados = 0;
            RandomAccessFile arquivoRegistros = null;
            try{
                arquivoRegistros = new RandomAccessFile(HASH_MASTER.getNomeArquivoHash(tabela),"r");
                while(true) {
                    Registro registro;
                    do {
                        registro = Registro.le(arquivoRegistros, tabela);
                    } while(registro.getFlag() == Registro.LIBERADO);                    
                    boolean selecionado = true;
                    //registro selecionado apenas se for filtrado por todos os filtros
                    for (Filtro f : filtros) {
                        //se registro não for filtrado por um filtro, então registro não é selecionado
                        if (!f.filtra(registro)) {
                            selecionado = false;
                            break;
                        }
                    }
                    if (selecionado) {
                        System.out.println(registro.toString(indexAtributosSelecionados));
                        System.out.println(Tabela.getLineSeparator(indexAtributosSelecionados.size()));
                        contMostrados++;
                    }
                }
            }
            catch (EOFException ex) {
                System.out.println("Consulta realizada com sucesso.");
                System.out.println("Foram mostrados "+contMostrados+" registros.");
            }
            finally {
                if(arquivoRegistros != null){
                    try {
                        arquivoRegistros.close();
                    } catch (IOException ex) {
                        System.out.println(ex.getMessage());
                    }
                }
            }
            
        }
        catch (IllegalArgumentException ex){
            System.out.println(ex.getMessage());
            System.out.println("Consulta de registros cancelada.");
        }
        catch (InputMismatchException ex) {
            System.out.println("[ERRO] O valor entrado eh invalido.");
            System.out.println("Consulta de registros cancelada.");
        }
        catch(FileNotFoundException ex) {
            System.out.println("[ERRO] O arquivo de registros da tabela desejada nao foi encontrado.");
            System.out.println("Consulta de registros cancelada.");
        }
        catch(IOException ex) {
            System.out.println("[ERRO] Falha na leitura do arquivo de registros da tabela desejada.");
            System.out.println("Consulta de registros cancelada.");
        }
    }
    
    /* Método criado só para testar rapidamente se o arquivo de registros estava sendo salvo e lido corretamente. Deletar depois de terminar o trabalho ou adicionar uma opção no menu para que isso seja mostrado.
    private static void opConsultarRegistros() {
        RandomAccessFile in = null;
        try {
            in = new RandomAccessFile("Testzim.dat", "r");
            while(true) {
                Registro r = Registro.le(in,getTabelaByName("Testzim"));
                System.out.println(r);
            }
        }
        catch (EOFException ex) {
        }
        catch(FileNotFoundException ex) {
        }
        catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            if(in != null) {
                try {
                    in.close();
                } catch (IOException ex) {
                    System.out.println(ex.getMessage());
                }
            }
        }
    }
    */    
    
    private static void opExcluirRegistros() {
        System.out.println("\n----------------------------\n");        
       
        try {
            //Escolha da tabela
            System.out.print("Entre com o nome da tabela da qual deseja excluir um registro: ");
            String nomeTabela = SCAN.next();
            Tabela tabela = getTabelaByName(nomeTabela);
            //Se não existe tabela no banco de dados com o nome desejado, cancela a exclusão do registro
            if(tabela == null) {
                throw new IllegalArgumentException("[Erro] Não existe tabela com esse nome.");
            }
            
            //Criação dos filtros
            List<Filtro> filtros;
            boolean prosseguir;
            do {
                filtros = Filtro.menuCriacaoFiltros(tabela);
                if(filtros.isEmpty()) {
                    System.out.print("[AVISO] Isso ira excluir todos os registro dessa tabela. Deseja prosseguir? (s/n): ");
                    prosseguir = SGBD.SCAN.next().startsWith("s");
                }
                else {
                    prosseguir = true;
                }
            } while(!prosseguir);
                        
            //Exclusão dos registros filtrados
            int contExcluidos = 0;
            RandomAccessFile arquivoRegistros = null;
            try{
                arquivoRegistros = new RandomAccessFile(HASH_MASTER.getNomeArquivoHash(tabela),"r");
                while(true) {
                    Registro registro;
                    do {
                        registro = Registro.le(arquivoRegistros, tabela);
                    } while(registro.getFlag() == Registro.LIBERADO);                    
                    boolean selecionado = true;
                    //registro selecionado apenas se for filtrado por todos os filtros
                    for (Filtro f : filtros) {
                        //se registro não for filtrado por um filtro, então registro não é selecionado
                        if (!f.filtra(registro)) {
                            selecionado = false;
                            break;
                        }
                    }
                    if (selecionado) {
                        HASH_MASTER.exclui(registro.getValorChave(), tabela);
                        contExcluidos++;
                    }
                }
            }
            catch (EOFException ex) {
                System.out.println("Exclusão realizada com sucesso.");
                System.out.println("Foram excluidos "+contExcluidos+" registros.");
            }
            finally {
                if(arquivoRegistros != null){
                    try {
                        arquivoRegistros.close();
                    } catch (IOException ex) {
                        System.out.println(ex.getMessage());
                    }
                }
            }
                        
        }
        catch (IllegalArgumentException ex){
            System.out.println(ex.getMessage());
            System.out.println("Exclusão de registro cancelada.");
        }
        catch (InputMismatchException ex) {
            System.out.println("[ERRO] O valor entrado é inválido.");
            System.out.println("Exclusão de registro cancelada.");
        }
        catch(FileNotFoundException ex) {
            System.out.println("[ERRO] O arquivo de registros da tabela desejada não foi encontrado.");
            System.out.println("Exclusão de registro cancelada.");
        }
        catch(IOException ex) {
            System.out.println("[ERRO] Falha na leitura do arquivo de registros da tabela desejada.");
            System.out.println("Exclusão de registro cancelada.");
        }
    }
    private static void opModificarRegistro() {
        //TODO: Implementar função
    }
    
    // Retorna uma tabela pelo nome dela
    private static Tabela getTabelaByName(String nomeTabela) {
        boolean found = false;
        Tabela tabela = null;
        DataInputStream in = null;
        try {
            in = new DataInputStream(new BufferedInputStream(new FileInputStream(ARQUIVO_CATALOGO)));
            while(!found) {
                tabela = Tabela.le(in);
                if(tabela.getNome().equalsIgnoreCase(nomeTabela))
                    found = true;
            }
        } catch (EOFException ex) {
        } catch(FileNotFoundException ex) {
            System.out.println(ex.getMessage());
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            if(in != null) {
                try {
                    in.close();
                } catch (IOException ex) {
                    System.out.println(ex.getMessage());
                }
            }
        }
        if(found)
            return tabela;
        else
            return null;
    }
}
