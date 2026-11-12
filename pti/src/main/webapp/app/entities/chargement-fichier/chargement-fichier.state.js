(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
            .state('chargement-fichier', {
                parent: 'entity',
                url: '/chargement-fichier?page&sort&search',
                data: {
                    authorities: ['ROLE_USER'],
                    pageTitle: 'ptiApp.chargementFichier.home.title'
                },
                views: {
                    'content@app': {
                        templateUrl: 'app/entities/chargement-fichier/chargement-fichier.html',
                        controller: 'ChargementFichierController',
                        controllerAs: 'vm'
                    }
                },
                resolve: {
                    translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                        $translatePartialLoader.addPart('chargementFichier');
                        $translatePartialLoader.addPart('global');
                        return $translate.refresh();
                    }]
                }
            })
            .state('chargement-fichier-valide', {
                parent: 'entity',
                url: '/chargement-fichier-valide',
                data: {
                    authorities: ['ROLE_USER'],
                    pageTitle: 'ptiApp.chargementFichier.home.title'
                },
                views: {
                    'content@app': {
                        templateUrl: 'app/entities/chargement-fichier/chargement-fichier.html',
                        controller: 'ChargementFichierController',
                        controllerAs: 'vm'
                    }
                },
                resolve: {
                    translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                        $translatePartialLoader.addPart('chargementFichier');
                        $translatePartialLoader.addPart('global');
                        return $translate.refresh();
                    }]
                }
            })
    }
})();