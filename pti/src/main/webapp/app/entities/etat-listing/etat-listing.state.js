(function() {
    'use strict';
    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
            .state('etat-listing', {
                parent: 'entity',
                url: '/etat-listing',
                data: {
                    authorities: ['ROLE_USER'],
                    pageTitle: 'EtatsListing'
                },
                views: {
                    'content@app': {
                        templateUrl: 'app/entities/etat-listing/etat-listing.html',
                        controller: 'EtatListingController',
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
