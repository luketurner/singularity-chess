(ns ^{:doc "Game component contains all game logic (move validity, piece info, etc.).
            Provides functions for reading and modifying various aspects of the current game state."}
  singularity.game
	(:require [singularity.db :as db]
            [cljs.core.match :refer-macros [match]]))

(defn col-len [x] (+ 5 (* 2 (if (> x 3) (- 7 x) x))))

(defn init!
  "Describes how the board should be laid out at the
   beginning of the game."
   []
  (let [w (fn [t] {:color :white :type t})
        b (fn [t] {:color :black :type t})
        pieces (into {
          [0, 0]  (w :rook) [1, 0] (w :knight) [2, 0] (w :bishop) [3, 0]  (w :queen)
          [4, 0]  (w :king) [5, 0] (w :bishop) [6, 0] (w :knight) [7, 0]  (w :rook)
          [0, 4]  (b :rook) [1, 6] (b :knight) [2, 8] (b :bishop) [3, 10] (b :queen)
          [4, 10] (b :king) [5, 8] (b :bishop) [6, 6] (b :knight) [7, 4]  (b :rook)}
          (conj (for [x (range 8)] [[x, 1] (w :pawn)])
                (for [x (range 8)] [[x, (-> x col-len (- 2))] (b :pawn)])))]
    (db/define
      :turn :white
      :board
        (into {} 
          (for [x (range 8)] (for [y (range (col-len x))]
            [[x y] {:piece (pieces [x y])}]))))))

(defn- straight? [diff] (some #{0} diff))

(defn diagonal? [[ax ay] [bx by]] 
  (let [offset (- (col-len bx) (col-len ax))
        by (- by offset)]
    (= (Math/abs (- bx ax)) (Math/abs (- by ay)))))

(defn valid-move?
  "Determines whether the move is valid for the piece, according
   to the rules of Singularity Chess movement. Because the validity
   of a move depends on the state of the board (e.g. pawn capturing),
   this function needs you to pass it the whole board."
  [a b]
  (and (some? a) (some? b) (not (= a b))
    (let [board (db/get :board)
          piece (get-in board [a :piece])
          signed-diff (map - b a)
          diff (map Math/abs signed-diff)]
      (and 
        (some? piece) ; must be a piece to move 
        (nil? (get-in board [b :piece])) ; landing spot must be empty
        (= (db/get :turn) (:color piece)) ; has to be my turn to move
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
              (match signed-diff
                [0 direction] true
                [(:or 0 1) direction] (some? (get-in board [b :piece]))
                :else false)))))))

(defn toggle-select!
  "Toggles selection of given space"
  [coords]
  (db/update-in [:selected-space] #(if (= % coords) nil coords)))

(defn move!
  "Move function accepts 2 coords, moves from s1 to s2"
  [s1 s2]
  (db/assoc-in [:board s2 :piece] (db/get-in [:board s1 :piece]))
  (db/assoc-in [:board s1 :piece] nil))