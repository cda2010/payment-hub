(function() {
    'use strict';
    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
            .state('etat-client', {
                parent: 'entity',
                url: '/etat-client',
                data: {
                    authorities: ['ROLE_USER'],
                    pageTitle: 'Etats'
                },
                views: {
                    'content@app': {
                        templateUrl: 'app/entities/etat-client/etat-client.html',
                        controller: 'EtatClientController',
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
