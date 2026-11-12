(function () {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ClientController', ClientController);

    ClientController.$inject = ['$state', 'TableService', '$scope', 'Client','Nationalite','$uibModal','ClientByTypePersonne','Document','BASE_URL_API','Utils','Notification','RequestUtils'];

    function ClientController($state, TableService, $scope, Client,Nationalite,$uibModal,ClientByTypePersonne,Document,BASE_URL_API,Utils,Notification,RequestUtils) {
        var vm = this;
        vm.generateAll = generateAll;
        var URL = BASE_URL_API + "etat/";
        vm.typeP = $state.$current.data.typeP;
        vm.nationalites = Nationalite.query();
        vm.canShowResultCount=false;
        vm.filtreCtrl=false;
        vm.format=null;
        vm.totalClientAll=0;
        vm.totalClient=0;


        vm.details = function (event, client, isViewing) {
            console.log('update',client);
            $state.go("client-souscription", {
                datas: client,
                isViewing: isViewing,
                back: $state.current.name
            });
        };

        vm.itemSearch = {
            "prenom": null,
            "nom": null,
            "raisonSociale": null,
            "telephone": null,
            "code": null,
            "adresse": null,
            "numeroPiece": null
        };
        vm.filtrer = true;
        ClientByTypePersonne.countClient({typePersonne: vm.typeP}, function (result) {
            console.log('total---',result.total);
            vm.totalItems = result.total + 1;
            vm.totalClientAll = result.total;
        });

        vm.initFiltre = function () {
            vm.filtrer = !vm.filtrer;
            if (!vm.filtrer) {
                vm.itemSearch = {
                    "prenom": null,
                    "nom": null,
                    "raisonSociale": null,
                    "telephone": null,
                    "code": null,
                    "adresse": null,
                    "numeroPiece": null
                };
                init();
            }
        };
        vm.filtre = function () {
            console.log("filtre des informations en cours");
            vm.filtreCtrl=true;
            init();
        };

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


            var entityParams = function () {
                return {
                    "telephone": vm.itemSearch.telephone,
                    "code": vm.itemSearch.code,
                    "nom": vm.itemSearch.nom,
                    "prenom": vm.itemSearch.prenom,
                    "adresse": vm.itemSearch.adresse,
                    "raisonSociale": vm.itemSearch.raisonSociale,
                    "typePersonne": vm.typeP,
                    "numeroPiece": vm.itemSearch.numeroPiece,
                };
            };
            var datas = {
                scope: $scope,
                vm: vm,
                //entity: ClientByTypePersonne,
                entity: RequestUtils.GetListClientOptimise,
                //entitySearch: ClientByTypePersonne,
                entitySearch: RequestUtils.GetListClientOptimise,
                dialogData: dialogData,
                entitySearchParams: entitySearchParams,
                entityParams: entityParams
            };
            TableService.init(datas);
            vm.canShowResultCount = true;
        }

        vm.detailsPersonne = function (datas) {
            console.log('data',datas);
            $uibModal.open({
                templateUrl: 'app/entities/personne/personne-detail.html',
                controller: 'PersonneDetailController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'lg',
                resolve: {
                    entity: function () {
                        return datas;
                    }
                }
            }).result.then(function () {
                //init()
            }, function () {
            });
        };
        vm.etats = [
            {
                ident: 1,
                url: URL + 'listing/client/filtre',
                type: 'CLIENT'
            }

        ];
        vm.item = {
            telephone: null,
            addresse: null,
            code: null,
            type: null,
            prenom: null,
            raisonSociale: null,
            nom: null,
            format: null,
            numeroPiece: null,
            statut: null
        };

        function generateAll() {

            vm.etat = _.find(vm.etats, {'ident': 1});
            vm.item.telephone=vm.itemSearch.telephone;
            vm.item.code= vm.itemSearch.code;
            vm.item.addresse=vm.itemSearch.adresse;
            vm.item.prenom=vm.itemSearch.prenom;
            vm.item.raisonSociale=vm.itemSearch.raisonSociale;
            vm.item.nom=vm.itemSearch.nom;
            vm.item.type=vm.typeP;
            vm.item.format=vm.format;
            vm.item.numeroPiece=vm.itemSearch.numeroPiece;
            vm.item.statut='NOETAT';
            console.log(vm.item);
            if(vm.format===null || vm.format===''){
                console.log('f',vm.format);
                vm.item.format = 'PDF';
            }
            if(vm.item.format ==='PDF' || vm.item.format ===null)
            {
                Utils.postPrintUrl(vm.etat.url,vm.item);
            } else {
                if(vm.typeP==='PHYSIQUE') {
                    if (vm.item.format === 'XLSX') {
                        vm.name = 'ExportClientPhysique.xlsx';
                    }
                    if (vm.item.format === 'CSV') {
                        vm.name = 'ExportClientPhysique.csv';
                    }
                } else {
                    if (vm.item.format === 'XLSX') {
                        vm.name = 'ExportClientMorale.xlsx';
                    }
                    if (vm.item.format === 'CSV') {
                        vm.name = 'ExportClientMorale.csv';
                    }
                }
                Utils.downloadFile(vm.etat.url,vm.item,vm.name);
            }
        }


        vm.deleteClient=function (client) {
            $uibModal.open({
                templateUrl: 'app/entities/client/client-delete-dialog.html',
                controller: 'ClientDeleteController',
                controllerAs: 'vm',
                size: 'md',
                resolve: {
                    entity: ['Client', function (Client) {
                        return client;
                    }]
                }
            }).result.then(function () {
                init();
                Notification.success("Suppression effectué !");
            }, function () {
            });
        }

    }
})();
