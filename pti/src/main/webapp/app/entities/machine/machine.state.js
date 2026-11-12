(function () {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
            .state('machine', {
                parent: 'entity',
                url: '/machine',
                data: {
                    authorities: ['ROLE_CONFIG_MACHINE']
                },
                views: {
                    'content@app': {
                        templateUrl: 'app/entities/machine/machines.html',
                        controller: 'MachineController',
                        controllerAs: 'vm'
                    }
                },
                resolve: {
                    translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                        $translatePartialLoader.addPart('global');
                        $translatePartialLoader.addPart('machineAuditEvent');
                        $translatePartialLoader.addPart('statut');
                        return $translate.refresh();
                    }]
                }
            }).state('verif-token', {
                parent: 'entity',
                url: '/verification-token',
                data: {
                    authorities: ['ROLE_USER']
                },
                views: {
                    'content@': {
                        templateUrl: 'app/entities/machine/machine-verif-token.html',
                        controller: 'MachineController',
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
