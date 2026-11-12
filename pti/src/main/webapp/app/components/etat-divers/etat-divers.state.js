(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
            .state('etat-divers', {
                parent: 'entity',
                url: '/etat-divers',
                data: {
                    authorities: [],
                    pageTitle: 'ptiApp.compte.home.title'
                },
                views: {
                    'content@app': {
                        templateUrl: 'app/components/etat-divers/etat-divers.html',
                        controller: 'EtatDiversController',
                        controllerAs: 'vm'
                    }
                },
                params: {},
                resolve: {}
            });
    }

})();
