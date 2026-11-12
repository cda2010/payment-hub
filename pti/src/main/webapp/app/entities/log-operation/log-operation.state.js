(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('log-operation', {
            parent: 'entity',
            url: '/log-operation',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.logOperation.home.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/log-operation/log-operations.html',
                    controller: 'LogOperationController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('logOperation');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        });
    }

})();
