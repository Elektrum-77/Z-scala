# Z-scala

## Introduction

On est un représenter par un rectangle vert.
On peut utiliser une armes pour tirer des cercles blanc.
L'objectif est de survivre a des vagues de rectangles rouges (aka zombies).

## Comment jouer

[Une démo est disponible ici](TODO)

On peut se déplacer avec les touches "zsqd" et/ou les flèches.
Clique gauche permet de tirer (on peut spammer ou rester appuyer).
La touche r permet de recharger (on peut spammer ou rester appuyer).

## Features

- déplacer le "rectangle" vert
- tirer (des cercles blancs)
- recharger, un chargeur fait 10 tirs et y a 100 tirs de dispo
- recharger prend quelque seconde
- tirer prend quelque seconde
- il est possible d'avoir du tir en rafale
- il est possible de changer beaucoup de valeurs lié a l'arme
  - vitesse de tir
  - vitesse de rafale
  - nombre de tir de rafale
  - nombre de tirs
  - nombre de tirs dans un chargeur
- il est possible de changer beaucoup de valeurs lié au tir 
  - vitesse de déplacement,
  - distance max,
  - dégâts,
  - pénétration
- des zombies apparaissent toutes les 10 secondes en groupe de 10 en cercle autour du joueur de façon aléatoire (configurables)
- les zombies peuvent être tués par les tirs
- le joueur peut mourir si il est trop souvent au contact des zombies

## Amélioration

- permettre d'annuler un rechargement
- recharger automatiquement
- des ennemies de différent type (le code est pret mais la fonctionnalité n'est pas encore ajouter)
- mieux gérer les déplacements des zombies
  - éviter qu'il se regroupe
  - réduire la taille du mouvement si > a la distance entre le joueur et le zombie
- différentes armes (comme pour les zombies le code est pret mais rien n'est ajouter)
- améliorer le HUD :
  - réduire la taille du texte sans le rendre illisible
  - ajouter les informations manquantes
    - point de vie des zombies
    - temps de rechargement
- réfléchir a un système de récompense
  - gagner des munitions
  - changer d'arme
- revoir la courbe de difficulté
