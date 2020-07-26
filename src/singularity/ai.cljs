(ns ^{:doc "Logic and query functions for A.I. opponent."}
  singularity.ai
  (:require [singularity.db :as db]
            [singularity.game :as game]
            [cljs.core.match :refer-macros [match]]))

(defn nilmin
  "Returns (min a b), except that `nil` is treated as positive infinity."
  [a b]
  (match [a b] [nil nil] nil [_ nil] a [nil _] b :else (min a b)))

(defn- minimax
  "A functional version of the minimax algorithm with alpha-beta pruning.
   Returns a tuple [score, state] where 'state' is the winning move for the
   turn, and 'score' is that state's minimax score. Eexpects to be passed an
   'h' function (which should accept one node and return a number) as well as a
   'gen-nodes' function which should accept a node and return a seq of nodes.
   Other parameters are similar to those found in any other minimax implementation."
  [h gen-nodes node depth alpha beta my-turn? past-nodes]
  (let [child-nodes (gen-nodes node)]
    ; if we reach our depth, use heuristic to estimate value of current state.
    (if (or (= depth 0) (empty? child-nodes))
      [(h node) (first past-nodes)]
      ; otherwise, recursively examine each child turn. Note cmp-fn is conditionally
      ; set based on my-turn?, which avoids branching and keeps code DRY as possible.
      (let [cmp-fn (if my-turn? (partial max alpha) (partial nilmin beta))]
        (loop [child-nodes child-nodes
               result nil]
          (let [score (first result)
                alpha (if my-turn? (cmp-fn score) alpha)
                beta (if my-turn? beta (cmp-fn score))]
            ; if alpha >= beta, move is worse than some other possible move, so break
            ; the loop and return the score. we use 'nil' instead of +/- infinity.
            (if (and (some? alpha) (some? beta) (>= alpha beta))
              result
              (recur
                (rest child-nodes)
                (cmp-fn (minimax h gen-nodes
                                 (first child-nodes)
                                 (dec depth)
                                 alpha beta
                                 (not my-turn?)
                                 (conj past-nodes (first child-nodes))))))))))))

(defn move
  "Returns a valid turn for the A.I. expressed as a coordinate tuple (from, to).
   If there are no valid moves, or if it is not the A.I.s turn, returns nil."
  []
  (let [depth (db/get :ai-depth)
        board (db/get :board) ; current board state is used as root node
        them (db/get :me) ; since I am AI, the human player is "them"
        me (case them :white :black :black :white)
        space-score #(let [c (get-in % [1 :piece :color])]
                      (*
                        (cond (= c me) 1 (= c them) -1 true 0)
                        (game/piece-scores (get-in % [1 :piece :type]))))
        board-score #(reduce + (map space-score %))
        next-moves #(map (partial apply (partial game/move %)) (game/available-moves % me))]
    ; call into the implementation to get a turn.
    (if (= (db/get :turn) me)
      (print (board-score board))
      (minimax board-score next-moves board depth nil nil true []))))

(defn init!
  "Initializes AI configuration settings."
  []
  (db/define :ai-depth 1))