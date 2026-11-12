(function() {
    'use strict';
    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
            .state('etat-balance', {
                parent: 'entity',
                url: '/etat-balance',
                data: {
                    authorities: ['ROLE_USER'],
                    pageTitle: 'EtatsBalance'
                },
                views: {
                    'content@app': {
                        templateUrl: 'app/entities/etat-balance/etat-balance.html',
                        controller: 'EtatBalanceController',
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
