package org.example;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Source;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class Main {
    private static Map<String, String> getLanguageOptions() {
        Map<String, String> options = new HashMap<>();

        options.put("js.ecmascript-version", "2023");
        options.put("js.top-level-await", "true");
        options.put("js.webassembly", "true");
        //options.put("js.commonjs-require", "true");
        options.put("js.mle-mode", "true");
        options.put("js.esm-eval-returns-exports", "true");
        options.put("js.unhandled-rejections", "throw");
        //options.put("js.commonjs-require-cwd", Paths.get("./src/main/resources").toAbsolutePath().toString());
        return options;
    }
    public static void main(String[] args) {

        try (Context context = Context.newBuilder("js", "wasm")
                .allowHostAccess(HostAccess.ALL)
                .allowIO(true)
                .option("engine.WarnInterpreterOnly", "false")
                .option("js.text-encoding", "true")
                .option("js.unhandled-rejections", "throw")
                .allowAllAccess(true)
                .allowHostClassLookup(s -> true)
                .options(getLanguageOptions())
                .build()) {
            byte[] wasmfile = Files.readAllBytes(Paths.get("./src/main/resources/mupdf-wasm.wasm"));
            //byte[] pdd = Files.readAllBytes(Paths.get("output.pdf"));
            context.getBindings("js").putMember("wasmBinary", wasmfile);
            context.eval(Source.newBuilder("js", Main.class.getResource("/polyfills.js"))
                    .mimeType("application/javascript+module")
                    .build());
            context.eval(Source.newBuilder("js", Main.class.getResource("/mud.js"))
                    .mimeType("application/javascript+module")
                    .build());

            /*
            Value jsBuffer = context.getPolyglotBindings().getMember("Buffer");
            int length = (int) jsBuffer.getArraySize();
            byte[] byteArray = new byte[length];
            for (int i = 0; i < length; i++) {
                int val = jsBuffer.getArrayElement(i).asInt();
                byteArray[i] = (byte) (val & 0xFF);
            }
            Files.write(Paths.get("output.pdf"), byteArray); */
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}