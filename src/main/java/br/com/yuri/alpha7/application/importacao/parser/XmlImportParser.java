package br.com.yuri.alpha7.application.importacao.parser;

import br.com.yuri.alpha7.application.importacao.model.ImportRecord;
import br.com.yuri.alpha7.domain.exception.ImportException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementação de {@link ImportParser} para arquivos XML.
 *
 * <p>Formato esperado:
 * <pre>{@code
 * <livros>
 *   <livro>
 *     <titulo>Clean Code</titulo>
 *     <isbn>9780132350884</isbn>
 *     <autores>Robert C. Martin</autores>
 *     <editora>Prentice Hall</editora>
 *     <dataPublicacao>2008</dataPublicacao>
 *     <idioma>eng</idioma>
 *     <numeroPaginas>431</numeroPaginas>
 *   </livro>
 * </livros>
 * }</pre>
 *
 * <p>Campos opcionais podem ser omitidos ou deixados em branco.
 * Múltiplos autores devem ser separados por ponto-e-vírgula ({@code ;}) dentro da tag {@code <autores>}.
 */
public class XmlImportParser implements ImportParser {

    @Override
    public String supports() {
        return "xml";
    }

    @Override
    public List<ImportRecord> parse(InputStream stream) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl",
                    true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities",
                    false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
                    factory.setExpandEntityReferences(false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(stream);
            Element root = doc.getDocumentElement();
            root.normalize();

            NodeList nodes = doc.getElementsByTagName("livro");
            List<ImportRecord> result = new ArrayList<>();
            for (int i = 0; i < nodes.getLength(); i++) {
                Element el = (Element) nodes.item(i);
                // Only direct children of root; nested <livro> inside fields are ignored
                if (el.getParentNode() != root) {
                    continue;
                }
                result.add(new ImportRecord(
                        text(el, "titulo"),
                        text(el, "isbn"),
                        text(el, "autores"),
                        text(el, "editora"),
                        text(el, "dataPublicacao"),
                        text(el, "idioma"),
                        text(el, "numeroPaginas")
                ));
            }
            return result;
        } catch (ImportException e) {
            throw e;
        } catch (Exception e) {
            throw new ImportException("Erro ao ler arquivo XML: " + e.getMessage(), e);
        }
    }

    /**
     * Extrai o texto do primeiro elemento filho com a tag informada.
     *
     * @param el  elemento {@code <livro>} sendo lido
     * @param tag nome da tag filha a buscar
     * @return texto do elemento, sem espaços nas bordas, ou string vazia se a tag não existir
     */
    private String text(Element el, String tag) {
        NodeList nodes = el.getElementsByTagName(tag);
        if (nodes.getLength() == 0) {
            return "";
        }
        return nodes.item(0).getTextContent().trim();
    }
}
