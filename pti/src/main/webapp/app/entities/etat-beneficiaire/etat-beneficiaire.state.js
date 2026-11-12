(function() {
    'use strict';
    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
            .state('etat-beneficiaire', {
                parent: 'entity',
                url: '/etat-beneficiaire',
                data: {
                    authorities: ['ROLE_USER'],
                    pageTitle: 'Etats beneficiaire'
                },
                views: {
                    'content@app': {
                        templateUrl: 'app/entities/etat-beneficiaire/beneficiaire.html',
                        controller: 'BeneficiaireController',
                        controllerAs: 'vm'
                    }
                },
                resolve: {
                    translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                        $translatePartialLoader.addPart('global');
                        return $translate.refresh();
                    }]
                }
            })
            .state('compte-bloque-cloture', {
                parent: 'entity',
                url: '/compte-bloque-cloture',
                data: {
                    authorities: ['ROLE_USER'],
                    pageTitle: 'Etats beneficiaire'
                },
                views: {
                    'content@app': {
                        templateUrl: 'app/entities/etat-beneficiaire/compte-bloque-cloture.html',
                        controller: 'CompteBloqueClotureController',
                        controllerAs: 'vm'
                    }
                },
                resolve: {
                    translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                        $translatePartialLoader.addPart('global');
                        return $translate.refresh();
                    }]
                }
            });
    }

})();
