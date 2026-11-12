(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('tableau-cumul', {
            parent: 'entity',
            url: '/tableau-cumul',
            data: {
                authorities: ['ROLE_CONFIG_TABLEAU_CUMUL'],
                pageTitle: 'ptiApp.tableauCumul.home.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/components/tables/tables.html',
                    controller: 'TableauCumulController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('tableauCumul');
                    $translatePartialLoader.addPart('temps');
                    $translatePartialLoader.addPart('action');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        });
    }

})();
