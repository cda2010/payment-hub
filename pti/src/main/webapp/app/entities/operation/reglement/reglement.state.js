(function() {
    'use strict';
    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
            .state('reglement', {
                parent: 'entity',
                url: '/reglement',
                data: {
                    authorities: ['ROLE_USER']
                }, views: {
                    'content@app': {
                        templateUrl: 'app/entities/operation/reglement/reglement.html',
                        controller: 'ReglementController',
                        controllerAs: 'vm',
                    }
                },
                resolve: {
                    entity: function () {
                        return {
                            codeRetrait: null,
                            uoAcquereurId: null,
                            canalLibelleCanal: null,
                            code:null,
                            id: null
                        };
                    },
                    isViewing: function () {
                        return false;
                    },
                    $uibModalInstance: function () {

                    },
                    translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                        $translatePartialLoader.addPart('operation');
                        $translatePartialLoader.addPart('client');
                        $translatePartialLoader.addPart('global');
                        return $translate.refresh();
                    }]
                }
            });
    }

})();
