(ns ^:figwheel-always singularity.view
  (:require [singularity.tools :as tools]
            [singularity.db :as db]
            [singularity.style :as style]))

; toggle selection of a space
(defn toggle-select! [x y] (db/update-in [:selected-space] #(if (= % [x y]) nil [x y])))

; triggered when a square is clicked
(defn onclick [x y]
  (toggle-select! x y))

(defn text [x y] (db/get-in [:board [x y] :piece]))

(defn space [board-width x y]
  (let [[path-d, [text-x, text-y]] (tools/space-calc board-width x y)]
    [:g
    [:path 
      {:stroke "#9999aa"
       :fill (if (= (db/get :selected-space) [x y]) "yellow" (if (= 1 (+ (mod x 2) (mod y 2))) "#c6d6f6" "#f0f0f0"))
       :d path-d
       :on-click #(onclick x y)}]
    [:text {:x text-x :y text-y :on-click #(onclick x y)} (text x y)]]))

(defn board []
  (let [width (db/get :board-width)]
    (print width)
    (into [:svg#board 
    {:width width :height (-> width (* 3) (/ 2)) :xmlns "http://www.w3.org/2000/svg"}]
    (for [x (range 8)] (for [y (range (tools/col-len x))]
      ^{:key [x y]} [space width x y])))))

(defn- input-group
  [path text type]
  (let [value (db/get-in path)
        handler #(db/assoc-in path (-> % .-target .-value))]
  [:div.input-group
    [:label text]
    [:input {:type type :value value :on-change handler}]]))

(defn panel []
  [:div.panel
    [:h1 "Singularity Chess"]
    [:p "A Chess variant in which the vertical rows are replaced by concentric circles."]
    [input-group [:board-width] "Board Width" "number"]])

(defn root [] 
  [:div [:style style/root] [board] [panel]])