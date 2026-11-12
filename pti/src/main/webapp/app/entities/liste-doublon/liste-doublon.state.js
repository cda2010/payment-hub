(function() {
    'use strict';
    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
            .state('liste-doublon', {
                parent: 'entity',
                url: '/liste-doublon',
                data: {
                    authorities: ['ROLE_USER'],
                    pageTitle: 'Liste doublon'
                },
                views: {
                    'content@app': {
                        templateUrl: 'app/entities/liste-doublon/liste-doublon.html',
                        controller: 'ListeDoublonController',
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
