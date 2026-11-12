(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ReleveCompteController', ReleveCompteController);

    ReleveCompteController.$inject = ['$state','$scope','ReleveCompte','TableService'];

    function ReleveCompteController($state,$scope, ReleveCompte, TableService) {

        var vm = this;

        init();
        function init(){
            var addItemData = {
                templateUrl: 'app/entities/releve-compte/releve-compte-dialog.html',
                controller: 'ReleveCompteDialogController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                    isViewing: function () {
                        return false;
                    },
                    isEdit: function () {
                        return false;
                    },
                    entity: function () {
                        return {};
                    }
                }
            };

            var editItemData = {
                templateUrl: 'app/entities/releve-compte/releve-compte-dialog.html',
                controller: 'ReleveCompteDialogController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                    isViewing: function () {
                        return false;
                    },
                    isEdit: function () {
                        return true;
                    },
                    entity: null //valeur déterminé à l'appel de la boite
                }
            };

            var detailItemData = {
                templateUrl: 'app/entities/releve-compte/releve-compte-detail.html',
                controller: 'ReleveCompteDetailController',
                controllerAs: 'vm',
                size: 'md',
                resolve: {
                    isViewing: function () {
                        return true;
                    },
                    isEdit: function () {
                        return false;
                    },
                    entity: null //valeur déterminé à l'appel de la boite,
                }
            };
            var deleteItemData = {
                templateUrl: 'app/entities/releve-compte/releve-comptes-delete-dialog.html',
                controller: 'ReleveCompteDeleteController',
                controllerAs: 'vm',
                size: 'sm',
                resolve: {
                    entity: null //valeur déterminé à l'appel de la boite
                }
            };
            var dialogData = {
                addData: addItemData,
                editData: editItemData,
                detailData: detailItemData,
                deleteData: deleteItemData
            };
            var entitySearchParams = function () {
                return {};
            };
            var entityParams = function () {
                return {
                };
            };
            var datas = {
                scope: $scope,
                vm: vm,
                entity: ReleveCompte,
                dialogData: dialogData,
                entitySearch: ReleveCompte,
                entitySearchParams: entitySearchParams,
                entityParams: entityParams,
                loadCallBack: vm.load
            };
            TableService.init(datas);

        }
    }
})();
