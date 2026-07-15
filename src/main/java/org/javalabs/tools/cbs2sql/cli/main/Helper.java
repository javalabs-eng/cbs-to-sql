package org.javalabs.tools.cbs2sql.cli.main;

import org.javalabs.tools.cbs2sql.cli.exec.ImportExecutor;
import org.javalabs.tools.cbs2sql.cli.exec.ExportExecutor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.javalabs.tools.cbs2sql.cli.exec.SchemaExecutor;

/**
 *
 * @author Sudiptasish Chanda
 */
public class Helper {
    
    private final Map<String, ExecutorBase> commands = new HashMap<>();
    
    public Helper() {
        commands.put("gen-schema", new SchemaExecutor());
        commands.put("cb-export", new ExportExecutor());
        commands.put("cb-import", new ImportExecutor());
    }
    
    public List<String> commands() {
        return new ArrayList<>(commands.keySet());
    }
    
    public ExecutorBase command(String name) {
        return commands.get(name);
    }
    
    public String welcome() {
        StringBuilder buff = new StringBuilder(128);
        
        buff.append("\n").append("###################################################################################");
        buff.append("\n").append("#                                                                                 #");
        buff.append("\n").append("#                         Couchbase To SQL Migration                              #");
        buff.append("\n").append("#                                                                                 #");
        buff.append("\n").append("# Commands:                                                                       #");
        buff.append("\n").append("#                                                                                 #");
        buff.append("\n").append(String.format("#  gen-schema          Generate the sql schema from couchbase doc                 #"));
        buff.append("\n").append(String.format("#  cb-export           Export the document and dump it to a local file            #"));
        buff.append("\n").append(String.format("#  cb-import           Import the documents to destination couchbase db           #"));
        buff.append("\n").append("#                                                                                 #");
        buff.append("\n").append("###################################################################################");
        buff.append("\n").append("\n").append("Type \"help\" to see the available commands");
        buff.append("\n").append("Use \"command help\" for usage of \"command\". [Example: schema help]").append("\n");
        
        //else {
        //    buff.append("Invalid command").append("\n");
        //    buff.append("If you are unsure of the command, type <command> help to see the list of options").append("\n");
        //}
        return buff.toString();
    }
    
    public String options(String command) {
        StringBuilder buff = new StringBuilder(512);
        
        if (command.equals("gen-schema")) {
            buff.append("\n").append(String.format("%-35s: %s", "Description", "Generate the sql schema from couchbase doc"));
            buff.append("\n").append(String.format("%-35s: %s", "Usage", "gen-schema [OPTIONS] ..."));
            buff.append("\n").append(String.format("%-35s: %s", "Example", "gen-schema -c <...> -h slc05mkt.org.com -p 8091 -u sudip -w s83@! -b resource"));
            buff.append("\n\n").append("The options are:");
            buff.append("\n\n");
            buff.append(String.format("%-40s %s\n", "-d [--data-set] <dataset_name>", "Name of the data set. If not provided, it will be derived from filename"));
            buff.append(String.format("%-40s %s\n", "-i [--in-dir] <directory_name>", "Directory where the json file(s) are kept"));
            buff.append(String.format("%-40s %s\n", "-f [--file-name] <filename>", "Json filename. If directory name is not specified, this should be a fully qualified name. Otherwise, it can be a simple name or extension (*.json)"));
            buff.append(String.format("%-40s %s\n", "-l [--flatten-child] [Y|N]", "If Y, eligible nested child objects are flattened into the parent table"));
            buff.append(String.format("%-40s %s\n", "-v [--verbose] [Y | N]", "Whether verbose logging will be enabled [Defau;t: N]"));
        }
        else if (command.equals("cb-export")) {
            buff.append("\n").append(String.format("%-35s: %s", "Description", "Export the documents of the remote couchbase db, as-is"));
            buff.append("\n").append(String.format("%-35s: %s", "Usage", "cb-export [OPTIONS] ..."));
            buff.append("\n").append(String.format("%-35s: %s", "Example", "cb-export -c cluster_1 -h slc05mkt.org.com -p 8091 -u sudip -w s83@! -b resource -s travel -l booking -d dataset_1,dataset_2 -n type -o /tmp"));
            buff.append("\n\n").append("The options are:");
            buff.append("\n\n");
            buff.append(String.format("%-40s %s\n", "-c [--cluster] <cluster_name>", "Couchbase cluster name"));
            buff.append(String.format("%-40s %s\n", "-h [--host] <host_name>", "Couchbase host [default: localhost]"));
            buff.append(String.format("%-40s %s\n", "-p [--port] <port>", "Couchbase server port [default: 8091]"));
            buff.append(String.format("%-40s %s\n", "-u [--user] <username>", "Couchbase db user name"));
            buff.append(String.format("%-40s %s\n", "-w [--password] <password>", "Couchbase db password"));
            buff.append(String.format("%-40s %s\n", "-b [--bucket] <bucket_name>", "Couchbase bucket to be queried"));
            buff.append(String.format("%-40s %s\n", "-s [--scope] <scope_name>", "Couchbase scope within a bucket to be queried [default: _default]"));
            buff.append(String.format("%-40s %s\n", "-l [--collection] <collection_name>", "Couchbase collection within a scope to be queried [default: _default]"));
            buff.append(String.format("%-40s %s\n", "-d [--data-set] <dataset_name>", "Comma separated datasets, or a filename with newline separated dataset"));
            buff.append(String.format("%-40s %s\n", "-n [--index-column] <idx_column>", "The secondary index column that differentiates the documents within a single bucket"));
            buff.append(String.format("%-40s %s\n", "-o [--out-dir] <dir_name>", "Directory name where the files will be dumped"));
            buff.append(String.format("%-40s %s\n", "-v [--verbose] [Y | N]", "Whether verbose logging will be enabled [Defau;t: N]"));
        }
        else if (command.equals("cb-import")) {
            buff.append("\n").append(String.format("%-35s: %s", "Description", "Export the documents of the remote couchbase db, as-is"));
            buff.append("\n").append(String.format("%-35s: %s", "Usage", "cb-import [OPTIONS] ..."));
            buff.append("\n").append(String.format("%-35s: %s", "Example", "cb-import -c cluster_1 -h slc05mkt.org.com -p 8091 -u sudip -w s83@! -b resource -s travel -l booking -i /tmp"));
            buff.append("\n\n").append("The options are:");
            buff.append("\n\n");
            buff.append(String.format("%-40s %s\n", "-c [--cluster] <cluster_name>", "Couchbase cluster name"));
            buff.append(String.format("%-40s %s\n", "-h [--host] <host_name>", "Couchbase host [default: localhost]"));
            buff.append(String.format("%-40s %s\n", "-p [--port] <port>", "Couchbase server port [default: 8091]"));
            buff.append(String.format("%-40s %s\n", "-u [--user] <username>", "Couchbase db user name"));
            buff.append(String.format("%-40s %s\n", "-w [--password] <password>", "Couchbase db password"));
            buff.append(String.format("%-40s %s\n", "-b [--bucket] <bucket_name>", "Couchbase bucket where the data to be inserted"));
            buff.append(String.format("%-40s %s\n", "-s [--scope] <scope_name>", "Couchbase scope within a bucket data to be inserted [default: _default]"));
            buff.append(String.format("%-40s %s\n", "-l [--collection] <collection_name>", "Couchbase collection within a scope data to be inserted [default: _default]"));
            buff.append(String.format("%-40s %s\n", "-i [--in-dir] <dir_name>", "Directory name from where the document files will be read"));
            buff.append(String.format("%-40s %s\n", "-f [--file-name] <file_name>", "Specific json file to be imported. If this option is null, then all the files from the --in-dir will be imported"));
            buff.append(String.format("%-40s %s\n", "-v [--verbose] [Y | N]", "Whether verbose logging will be enabled [Defau;t: N]"));
        }
        else {
            buff.append("Invalid command ").append(command).append("\n");
            buff.append("If you are unsure about the command, type help to see the list of commands").append("\n");
        }
        return buff.toString();
    }
}
