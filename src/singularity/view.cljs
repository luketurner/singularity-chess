(ns ^:figwheel-always singularity.view
  (:require [singularity.tools :as tools]
            [singularity.db :as db]
            [singularity.style :as style :refer [colors]]
            [singularity.handlers :as handlers]))

(defn- space
  "Component defining a single space."
  [board-width x y]
  (let [[path-d, [text-x, text-y]] (tools/space-calc board-width x y)
         handler #(handlers/space x y)
         text (tools/piece->str (db/get-in [:board [x y] :piece]))
         selected? (= (db/get :selected-space) [x y])
         black? (= 1 (+ (mod x 2) (mod y 2)))]
    [:g
      [:path 
        {:stroke (colors :sq-border)
         :fill (colors (if selected? :sq-sel (if black? :sq-black :sq-white)))
         :d path-d
         :on-click handler}]
      [:text {:x text-x :y text-y :on-click handler :fill (colors :sq-text)} text]]))

(defn- board
  "Component defining the whole chessboard."
  []
  (let [width (db/get :board-width)]
    (into [:svg#board {:width width
                       :height (-> width (* 3) (/ 2))
                       :xmlns "http://www.w3.org/2000/svg"}]
      (for [x (range 8)] 
        (for [y (range (tools/col-len x))]
          ^{:key [x y]} [space width x y])))))

(defn- input-group
  "Component defining an input (with a label) that is automatically mapped to
   given `path` in the `db`."
  [path text type]
  (let [value (db/get-in path)
        handler #(db/assoc-in path (-> % .-target .-value))]
  [:div.input-group
    [:label text]
    [:input {:type type :value value :on-change handler}]]))

(defn- panel
  "Component defining the config/info panel that appears alongside the board."
  []
  [:div.panel
    [:h1 "Singularity Chess"]
    [:p "A Chess variant in which the vertical rows "
        "are replaced with concentric circles. "
        "Play otherwise proceeds as normal for chess."]
    [input-group [:board-width] "Board Width" "number"]])

(defn root
  "Root component defining the whole application, including CSS."
  [] 
  [:div [:style style/root] [board] [panel]])