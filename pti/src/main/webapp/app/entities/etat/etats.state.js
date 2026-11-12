(function() {
    'use strict';
    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
            .state('etats', {
                parent: 'entity',
                url: '/etats',
                data: {
                    authorities: ['ROLE_USER'],
                    pageTitle: 'Etats'
                },
                views: {
                    'content@app': {
                        templateUrl: 'app/entities/etat/etats.html',
                        controller: 'EtatController',
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
