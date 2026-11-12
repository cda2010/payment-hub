(function() {
    'use strict';
    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
            .state('tableau-solde', {
                parent: 'entity',
                url: '/tableau-solde',
                data: {
                    authorities: ['ROLE_USER'],
                    pageTitle: 'tableau-solde.home.title'
                },
                views: {
                    'content@app': {
                        templateUrl: 'app/entities/tableau-solde/tableau-solde.html',
                        controller: 'TableauSoldeController',
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
