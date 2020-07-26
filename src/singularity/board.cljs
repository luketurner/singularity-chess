(ns ^{:doc "Contains functions for generating the Singularity Chess board."}
  singularity.board
  (:require [singularity.db :as db]
            [singularity.game :as game]))

(defn- join [& args] (clojure.string/join " " args))

(defn- space-calc
  "Calculates the SVG path for the space. It's complicated, but not
   computationally hard. Also calculates the [x, y] coordinates for the
   approximate center of the piece, where the SVG piece should be placed. 
   Return value is of the form [path-d, [x, y]]."
  [width [x y]]
  (let [[dx cx cy]    [(/ width 8) (/ width 2) (/ (* width 3) 4)]
        axis          (Math/floor (/ (game/col-len x) 2))
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
            center [((if left? - +) side (/ dx 2)) cy]]
        [path center])
      [(join
        "M" x0 y1
        "A" r0 r0 0 0 (if upper? 0 1) x1 y2
        "L" x1 y3
        "A" r1 r1 0 0 (if upper? 1 0) x0 y4
        "Z") [(- (/ (+ x0 x1) 2) 5) (/ (+ y1 y3) 2)]])))

(defn- piece->str
  "Returns a stringified version of the piece keyword, used for display.
   Currently, returns 2-character strings like WQ, WK, BQ, BK, etc."
  [piece]
  (if (nil? piece) ""
    (let [strings {:pawn "p" :bishop "b" :knight "k" :rook "r"
                   :queen "q" :king "k" :white "w" :black "b"}]
     (clojure.string/upper-case (str ((piece :color) strings) ((piece :type) strings))))))

(defn- onclick
  "Event handler for when user clicks a space. This can have one of two
  outcomes. If the click indicates a valid move for the selected piece,
  clicking will move the selected piece to the new location. If the
  movement is invalid or a piece is not selected, this will select the
  clicked piece."
  [coords]
  (let [selected @(db/selected-space)]
    (if (game/valid-move? selected coords)
      (do ; this is a valid move, use movement
        (game/move! selected coords)
        (game/toggle-select! nil))
      (game/toggle-select! coords)))) ; not a valid move, use selection

(defn- space
  "Component defining a single space."
  [board-width coords]
  (let [[path-d, [text-x, text-y]] (space-calc board-width coords)
         selected (db/get :selected-space)
         handler #(onclick coords)
         text (piece->str (db/get-in [:board coords :piece]))
         el-class (if (nil? selected) ""
                   (if (= selected coords) "sel" 
                     (if (nil? (db/get-in [:board selected :piece])) ""
                       (if (game/valid-move? selected coords) "valid" "invalid"))))]
    [:g
      [:path 
        {:d path-d
         :on-click handler
         :class el-class}]
      [:text {:x text-x :y text-y :on-click handler} text]]))

(defn board
  "Component defining the whole chessboard."
  []
  (let [width (db/get :board-width)]
    (into [:svg#board {:width width
                       :height (-> width (* 3) (/ 2))
                       :xmlns "http://www.w3.org/2000/svg"}]
      (for [x (range 8)] 
        (for [y (range (game/col-len x))]
          ^{:key [x y]} [space width [x y]])))))

(defn init!
  "Initializes the data this component relies on in 'db'."
  []
  (db/define       
    :board-width 600
    :selected-space nil
    :me :white
    :them :ai))