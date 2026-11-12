(function () {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ClientCreaController', ClientCreaController);

    ClientCreaController.$inject = ['$state', 'TableService', '$scope', 'Client','Nationalite'];

    function ClientCreaController($state, TableService, $scope, Client,Nationalite) {
        var vm = this;
        vm.nationalites = Nationalite.query();

        vm.details = function (event, client, isViewing) {
            $state.go("client-souscription", {
                datas: client,
                isViewing: isViewing
            });
        };

        vm.itemSearch = {
           "prenom": null,
           "nom": null,
           "telephone": null,
           "code": null
        };

        vm.initFiltre = function () {
            vm.filtrer = !vm.filtrer;
            if (!vm.filtrer) {
                vm.itemSearch = {
                    "telephone": null,
                    "code": null
                };
                init();
            }
        }
        vm.filtre = function () {
            console.log("filtre des informations en cours");
            init();
        }
        init();
        function init() {
            var addItemData = {};

            /**
             *Modification d'un élément
             */
            var editItemData = {};

            /**
             * Suppresion d'un élément
             */
            var deleteItemData = {};

            var detailItemData = {};

            var dialogData = {
                addData: addItemData,
                editData: editItemData,
                deleteData: deleteItemData,
                detailData: detailItemData
            };
            var entitySearchParams = function () {
                return {};
            };

 vm.detailsPersonne = function(datas){
            console.log('details des personnes .....');
            console.log(datas);
            $uibModal.open({
                templateUrl: 'app/entities/personne/personne-detail.html',
                controller: 'PersonneDetailController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'lg',
                resolve: {
                    entity : function(){
                        return datas;
                    }
                }
            }).result.then(function() {
            }, function() {
            }); 
        }
        
        
            var entityParams = function () {
                return {
                    "telephone": vm.itemSearch.telephone,
                    "code": vm.itemSearch.code,
                     "nom": vm.itemSearch.nom,
                    "prenom": vm.itemSearch.prenom
                };
            };
            var datas = {
                scope: $scope,
                vm: vm,
                entity: Client,
                entitySearch:Client,
                dialogData: dialogData,
                entitySearchParams: entitySearchParams,
                entityParams: entityParams
            };
            TableService.init(datas);
        }

    }
})();