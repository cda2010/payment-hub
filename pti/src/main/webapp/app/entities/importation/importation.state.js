(function() {
    'use strict';
    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
            .state('etat-importation', {
                parent: 'entity',
                url: '/importation',
                data: {
                    authorities: ['ROLE_USER'],
                    pageTitle: 'Importation'
                },
                views: {
                    'content@app': {
                        templateUrl: 'app/entities/importation/importation.html',
                        controller: 'ImportationController',
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
