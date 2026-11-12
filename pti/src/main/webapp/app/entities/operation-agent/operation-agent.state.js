(function() {
    'use strict';
    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
            .state('operation-agent', {
                parent: 'entity',
                url: '/operation-agent',
                data: {
                    authorities: ['ROLE_USER'],
                    pageTitle: 'EtatsListing'
                },
                views: {
                    'content@app': {
                        templateUrl: 'app/entities/operation-agent/operation-agent.html',
                        controller: 'OperationAgentController',
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
