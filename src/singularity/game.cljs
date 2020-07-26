(ns ^{:doc "Game component contains all game logic (move validity, piece info, etc.).
            Provides functions for reading and modifying various aspects of the current game state."}
  singularity.game
  (:require [singularity.db :as db]
            [cljs.core.match :refer-macros [match]]))

(defn col-len 
  "The length (height) of each column depends on its x-coordinate.
   This function calculates the length given the x-value for the square.
   e.g. (col-len 1) => 7"
  [x]
  (+ 5 (* 2 (if (> x 3) (- 7 x) x))))

(defn init!
  "Initializes values for a new game."
  []
  (let [w (fn [t] {:color :white :type t})
        b (fn [t] {:color :black :type t})
        pawns (concat (for [x (range 8)] [[x 1] (w :pawn)])
                      (for [x (range 8)] [[x (-> x col-len (- 2))] (b :pawn)]))
        nonpawns {[0 0]  (w :rook) [1 0] (w :knight) [2 0] (w :bishop) [3 0]  (w :queen)
                  [4 0]  (w :king) [5 0] (w :bishop) [6 0] (w :knight) [7 0]  (w :rook)
                  [0 4]  (b :rook) [1 6] (b :knight) [2 8] (b :bishop) [3 10] (b :queen)
                  [4 10] (b :king) [5 8] (b :bishop) [6 6] (b :knight) [7 4]  (b :rook)}
        pieces (into nonpawns pawns)
        board (into {} (for [x (range 8)
                             y (range (col-len x))]
                         [[x y]
                          {:piece (pieces [x y])}]))]
    (reset! (db/turn) :white)
    (reset! (db/board) board)))

(defn- straight? [diff] (some #{0} diff))

(defn diagonal? [[ax ay] [bx by]]
  (let [offset (- (col-len bx) (col-len ax))
        by (- by offset)]
    (= (Math/abs (- bx ax)) (Math/abs (- by ay)))))

(defn valid-move?
  "Determines whether the move is valid for the piece, according
   to the rules of Singularity Chess movement."
  ([a b] (valid-move? a b @(db/board)))
  ([a b board]
   (and (some? a) (some? b) (not (= a b))
     (let [piece (get-in board [a :piece])
           signed-diff (map - b a)
           diff (map Math/abs signed-diff)]
       (and 
         (some? piece) ; must be a piece to move 
         (nil? (get-in board [b :piece])) ; landing spot must be empty
         (= @(db/turn) (:color piece)) ; has to be my turn to move
         (case (:type piece) ; finally, we get to ascertaining if the move obeys
                            ; the rules of the game, which depends on the piece.
           :rook (straight? diff)
           :knight (match diff
                    [1 2] true
                    [2 1] true
                    :else false)
           :bishop (diagonal? a b)
           :queen (or (diagonal? a b) (straight? diff))
           :king (match diff
                  [(:or 0 1) (:or 0 1)] true
                  :else false)
           :pawn ; note, does not implement en passant.
             (let [direction (if (= :white (:color piece)) 1 -1)]
               (println "dir" direction signed-diff)
               (match (vec signed-diff)
                 [0 direction] true
                 [(:or 0 1) direction] (some? (get-in board [b :piece]))
                 :else false))))))))


(defn available-moves
  "Returns a set of all available moves on given board for given color."
  [board color]
  (for [[from from-v] board
        [to to-v] board
        :when (and (-> from-v :color (= color))
                   (valid-move? from to board))]
    [from to]))


(def piece-scores
  "Lists the point scores of each piece"
  {:king   10
   :queen  8
   :rook   5
   :knight 3
   :bishop 3
   :pawn   1})

(defn toggle-select!
  "Toggles selection of given space"
  [coords]
  (swap! (db/selected-space) #(if (= % coords) nil coords)))

(defn move
  "Move function accepts a board and 2 coords, moves from s1 to s2"
  [board s1 s2]
  (-> board 
    (assoc-in [s2 :piece] (get-in board [s1 :piece]))
    (assoc-in [s1 :piece] nil)))

(defn move!
  "Same as 'move', but applies the change to the db immediately."
  [s1 s2]
  (reset! (db/piece s2) @(db/piece s1))
  (reset! (db/piece s1) nil))