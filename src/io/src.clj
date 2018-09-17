;;(ns io.src)                                                 ;;namespace breaks 'eval' of this code

(println "indirect, clj: hello, world!")
(println (eval (read-string "(+ 2 4)")))