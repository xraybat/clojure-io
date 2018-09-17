(ns io.eval)

(require '[io.gulp-expectorate :as io])

;; using own 'gulp' and 'expectorate'
(io/expectorate-3 "out.clj" (io/gulp-3 "src.clj"))

;; execute code *form* (not text string) in var using 'eval'
(def x '(+ 2 3))
(println "x = " x)
(println (eval x))
(newline)

;; code in text or files for 'eval'; '(binding [*read-eval false]' (eval ...)) used as code-injection protection
;;(binding [*read-eval* false] (println (eval (read-string "(+ 2 4)"))))
(binding [*read-eval* false] (eval (read-string "(println \"indirect, inline: hello, world!\")")))
(newline)

;; only reads one (first) string??
(binding [*read-eval* false] (eval (read-string (io/gulp-3 "src.txt"))))             ;; works
(newline)

;; only reads one (first) string??
(println "read-string:" (read-string (io/gulp-3 "src.clj")))
(binding [*read-eval* false] (eval (read-string (io/gulp-3 "src.clj"))))             ;; works w/out '(ns)' at start of .clj
(newline)

;; clojure's 'slurp' and 'eval'?? no, it's not an 'eval'-able string...
(println "slurp:" (slurp "src.clj"))
(binding [*read-eval* false] (eval (slurp "src.clj")))
(newline)

;; clojure's 'load-file' and 'eval'; yesss...it works on all lines!
(binding [*read-eval* false] (eval (load-file "src.clj")))
(binding [*read-eval* false] (eval (load-file "src.txt")))
;; note: how 'load-file' works, but our own 'gulp' doesn't, prolly 'cos one's code and the other's a string only
;;(binding [*read-eval* false] (eval (io/gulp-3 "src.clj")))