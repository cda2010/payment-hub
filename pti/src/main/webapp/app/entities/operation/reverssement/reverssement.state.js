(function() {
    'use strict';
    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
            .state('reverssement', {
                parent: 'entity',
                url: '/reverssement',
                data: {
                    authorities: ['ROLE_CONFIG_MACHINE']
                },
                views: {
                    'content@app': {
                        templateUrl: 'app/entities/operation/reverssement/reverssement.html',
                        controller: 'ReverssementController',
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
