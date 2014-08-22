import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Classe que representa uma entidade genérica
 * Todas as entidades a serem manipuladas devem herdar desta classe
 * @author leomurta s2
 */
public interface Entidade {

    /*
     * Salva uma instância de uma entidade no arquivo out
     * @param out Arquivo onde a instância da entidade será salva
     */
    public void salva(DataOutputStream out) throws IOException;
}
