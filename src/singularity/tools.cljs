(ns singularity.tools)

; returns # of squares in given column.
(defn col-len [x] (+ 5 (* 2 (if (> x 3) (- 7 x) x))))

; combines args into a string, separated by spaces
(defn join [& args] (clojure.string/join " " args))

; calculates the "d" attribute for given space
; returns [d, [x1, x1]] where x1, y1 indicate the center of the space (for text)
(defn space-calc [width x y]
  (let [[dx cx cy]    [(/ width 8) (/ width 2) (/ (* width 3) 4)]
        axis          (Math/floor (/ (col-len x) 2))
        upper?        (< y axis)
        left?         (<= x 3)
        calc-y        (fn [x r] (->> r (/ (- cx x)) Math/acos Math/sin (* r) ((if upper? + -) cy)))
        dx            (/ width 8)
        r0            (* dx (+ (Math/abs (- x 4)) (Math/abs (- y axis))))
        r1            ((if left? - +) r0 dx)
        [x0 x1]       [(* x dx) (* (inc x) dx)]
        [y1 y2 y3 y4] [(calc-y x0 r0) (calc-y x1 r0) (calc-y x1 r1) (calc-y x0 r1)]]
    (if (= y axis)

      (let [sweep (if left? 1 0)
            side    (if left? x1 x0)
            y-off  (- cy (if left? y2 y4))
            radius (if left? r0 r1)
            path (join "M" side (+ cy y-off) "A" radius radius 0 0 sweep side (- cy y-off) "Z")
            center [((if left? - +) (/ dx 2) side) cy]]
        [path center])

      [(join
        "M" x0 y1
        "A" r0 r0 0 0 (if upper? 0 1) x1 y2
        "L" x1 y3
        "A" r1 r1 0 0 (if upper? 1 0) x0 y4
        "Z") [(- (/ (+ x0 x1) 2) 5) (/ (+ y1 y3) 2)]])))