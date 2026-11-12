(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('classification-facturier', {
            parent: 'entity',
            url: '/classification-facturier',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.classificationFacturier.home.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/classification-facturier/classification-facturiers.html',
                    controller: 'ClassificationFacturierController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('classificationFacturier');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('classification-facturier-detail', {
            parent: 'classification-facturier',
            url: '/classification-facturier/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.classificationFacturier.detail.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/classification-facturier/classification-facturier-detail.html',
                    controller: 'ClassificationFacturierDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('classificationFacturier');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'ClassificationFacturier', function($stateParams, ClassificationFacturier) {
                    return ClassificationFacturier.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'classification-facturier',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('classification-facturier-detail.edit', {
            parent: 'classification-facturier-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/classification-facturier/classification-facturier-dialog.html',
                    controller: 'ClassificationFacturierDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        entity: ['ClassificationFacturier', function(ClassificationFacturier) {
                            return ClassificationFacturier.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('classification-facturier.new', {
            parent: 'classification-facturier',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/classification-facturier/classification-facturier-dialog.html',
                    controller: 'ClassificationFacturierDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        entity: function () {
                            return {
                                libelleClassification: null,
                                uniteOrganisationId: null,
                                niveauSuperieurID: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('classification-facturier', null, { reload: 'classification-facturier' });
                }, function() {
                    $state.go('classification-facturier');
                });
            }]
        })
        .state('classification-facturier.edit', {
            parent: 'classification-facturier',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/classification-facturier/classification-facturier-dialog.html',
                    controller: 'ClassificationFacturierDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        entity: ['ClassificationFacturier', function(ClassificationFacturier) {
                            return ClassificationFacturier.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('classification-facturier', null, { reload: 'classification-facturier' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('classification-facturier.delete', {
            parent: 'classification-facturier',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/classification-facturier/classification-facturier-delete-dialog.html',
                    controller: 'ClassificationFacturierDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['ClassificationFacturier', function(ClassificationFacturier) {
                            return ClassificationFacturier.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('classification-facturier', null, { reload: 'classification-facturier' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
