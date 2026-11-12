(function() {
    'use strict';
    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
            .state('etat-compte', {
                parent: 'entity',
                url: '/etat-compte',
                data: {
                    authorities: ['ROLE_USER'],
                    pageTitle: 'Etats'
                },
                views: {
                    'content@app': {
                        templateUrl: 'app/entities/etat-compte/etat-compte.html',
                        controller: 'EtatCompteController',
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
