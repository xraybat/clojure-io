(ns io.io)

(require '[io.gulp-expectorate :as io])

(println
  (io/gulp "input.txt")
  )

(io/expectorate "output.txt" (io/gulp "input.txt"))
(io/expectorate-2 "output-2.txt" (io/gulp-2 "input.txt"))
(io/expectorate-3 "output-3.txt" (io/gulp-3 "input.txt"))

