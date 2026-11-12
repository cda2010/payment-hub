(function() {
    'use strict';
    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
            .state('etat-operation', {
                parent: 'entity',
                url: '/etat-operation',
                data: {
                    authorities: ['ROLE_USER'],
                    pageTitle: 'EtatsOperation'
                },
                views: {
                    'content@app': {
                        templateUrl: 'app/entities/etat-operation/etat-operation.html',
                        controller: 'EtatOperationController',
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
