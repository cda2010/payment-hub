(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('UniteOrganisationPaginerController', UniteOrganisationPaginerController);

    UniteOrganisationPaginerController.$inject = ['$scope','$rootScope','PopupService','$uibModal', 'UniteOrganisation','TypeUniteOrganisation', 'TableService','isConsult','RequestUtils','UniteOrganisationPaginer','BASE_URL_API','Utils','Ville','GroupeProfilActeur','Notification' ];

    function UniteOrganisationPaginerController($scope,$rootScope,PopupService,$uibModal, UniteOrganisation, TypeUniteOrganisation, TableService,isConsult,RequestUtils,UniteOrganisationPaginer,BASE_URL_API,Utils,Ville,GroupeProfilActeur,Notification ) {

        var vm = this;
        var URL = BASE_URL_API + "etat/listing/unite";
        vm.format=null;
        //vm.organisations = UniteOrganisation.query();
        //vm.organisation = [];
        vm.item={
            pereUOId:null,
            libelleUniteOrganisation:null,
            codeUO:null,
            numeroOrdre:null,
            typeUOLibelleTypeUO:null,
            pereUoId:null,
            groupeId:null,
            villeId:null,
            adresse:null,

        };
        vm.init=init;
        vm.cherch=cherch;
        vm.genererUO=genererUO;
        vm.canShowResultCount=false;
        vm.sendInfoUO=sendInfoUO;
        //attributs de filtrage
        vm.acti=false;
        vm.itemSearch = {
            'pereUOId':null,
            'libelleUniteOrganisation':'',
            'codeUO':'',
            'numeroOrdre':null,
            'typeUOLibelleTypeUO':null,
            'pereUoId':null,
            'groupeId':null,
            'villeId':null,
            'adresse':'',
            'format':null
        };
        vm.afficheFiltre=false;
        vm.afficherFiltre=afficherFiltre;


        function sendInfoUO(organisationId) {
            console.log('id',organisationId);
            UniteOrganisation.sendInfoUoSMS({organisationId:organisationId},function (response) {
                console.log(response);
                Notification.success("Information envoyée avec succès");
            },function (error) {
                console.log('error');
                Notification.error("Echec d\'envoi du message");
            })
        }


        function afficherFiltre(){
            vm.afficheFiltre=!vm.afficheFiltre;
            if(!vm.afficheFiltre){
                vm.itemSearch.libelleUniteOrganisation='';
                vm.itemSearch.codeUO='';
                vm.itemSearch.numeroOrdre=null;
                vm.itemSearch.typeUO=null;
                vm.itemSearch.groupeId=null;
                vm.itemSearch.villeId=null;
                vm.itemSearch.adresse='';
                vm.itemSearch.format= null;
                init();
            }
        }

        //valeurs de selections
        vm.typeuniteorganisations =[] ;
        vm.villes=Ville.query();
        vm.groupes=GroupeProfilActeur.queryByType({acteur:'ORGANISATION'});
        vm.FSEs=UniteOrganisation.queryByTypeUoFSE();
        console.log('FSE',vm.FSEs);
        TypeUniteOrganisation.queryOptimisetUo(function (typeUniteOrganisations) {
            console.log('og',typeUniteOrganisations);
            let i=0;
            for(i=0;i<typeUniteOrganisations.length;i++){
                if(typeUniteOrganisations[i].libelleTypeUO !="PAYS"
                    &&typeUniteOrganisations[i].libelleTypeUO !="FSE"
                    &&typeUniteOrganisations[i].libelleTypeUO !="ZONE"){
                    vm.typeuniteorganisations.push(typeUniteOrganisations[i]);
                }
            }
        });

        //loadAll();
        init();
        //***************************************************************************************
        vm.addChild = function(entity,niveau) {
            vm.init();
            vm.id=entity;
            vm.niveau = niveau;
            vm.addItem();
        };

        vm.detail = function(event,entity) {
            //vm.init();
            vm.detailItem(event,entity);
        };
        vm.edit = function(event,entity) {
            //vm.init();
            vm.editItem(event,entity);
        };
        vm.delete = function(event,entity) {
            //vm.init();
            vm.deleteItem(event,entity);
        };
       vm.editUO= function (organisationId) {
            $uibModal.open({
                templateUrl: 'app/entities/unite-organisation/unite-organisation-detail.html',
                controller: 'UniteOrganisationDetailController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'lg',
                resolve: {
                    entity: ['UniteOrganisation', function(UniteOrganisation) {
                        return UniteOrganisation.get({id : organisationId}).$promise;
                    }],
                }
            }).result.then(function() {
                loadAll();
            }, function() {
            });
        };
       function genererUO() {
           vm.item.fse=vm.itemSearch.pereUoId;
           vm.item.typeuo=vm.itemSearch.typeUO;
           vm.itemSearch.format = vm.format;
           console.log('data',vm.itemSearch);
           if(vm.format===null || vm.format===''){
               console.log('f',vm.format);
               vm.itemSearch.format = 'PDF';
           }
           if(vm.itemSearch.format ==='PDF' || vm.item.format ===null)
           {
               Utils.postPrintUrl(URL,vm.itemSearch);
           } else {
               if(vm.itemSearch.format ==='XLSX') {console.log('oui') ; vm.name='ExportuniteOrganisation.xlsx';}
               if(vm.itemSearch.format ==='CSV') {console.log('oui') ;vm.name='ExportuniteOrganisation.csv';}
               Utils.downloadFile(URL,vm.itemSearch,vm.name);
           }

       }
       function cherch() {
           init();
           vm.acti=true;

       }
        function init(){
            var addItemData = {
                templateUrl: 'app/entities/unite-organisation/unite-organisation-dialog.html',
                controller: 'UniteOrganisationDialogController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'lg',
                resolve: {
                    isViewing: function () {
                        return false;
                    },
                    isEdit: function () {
                        return false;
                    },
                    entity: function () {
                        return {
                            /*Initialisation des valeurs par défauts de l'entité à créer*/
                            id: null,
                            libelleUniteOrganisation: null,
                            niveau: null,
                            feuille: true,
                            estSiege: null,
                            codeUO: null,
                            numeroOrdre: null,
                            representantId: null,
                            pereUOId: vm.id,
                            pereNiveau: vm.niveau,
                            typeUOId: null,
                            fournisseurId: null,
                            serviceProfilId:null
                        };
                    }
                }
            };

            var editItemData = {
                templateUrl: 'app/entities/unite-organisation/unite-organisation-dialog.html',
                controller: 'UniteOrganisationDialogController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'lg',
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
                templateUrl: 'app/entities/unite-organisation/unite-organisation-dialog.html',
                controller: 'UniteOrganisationDialogController',
                controllerAs: 'vm',
                size: 'lg',
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
                templateUrl: 'app/entities/unite-organisation/unite-organisation-delete-dialog.html',
                controller: 'UniteOrganisationDeleteController',
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
                    libelleUniteOrganisation: vm.itemSearch.libelleUniteOrganisation,
                    codeUO:vm.itemSearch.codeUO,
                    numeroOrdre:vm.itemSearch.numeroOrdre,
                    typeUO:vm.itemSearch.typeUO,
                    pereUoId:vm.itemSearch.pereUoId,
                    groupeId:vm.itemSearch.groupeId,
                    villeId:vm.itemSearch.villeId,
                    adresse:vm.itemSearch.adresse
                };
            };

            var datas = {
                scope: $scope,
                vm: vm,
                entity:  RequestUtils.GetOrganisationPaginerOptimise,
                dialogData: dialogData,
                entitySearch:  RequestUtils.GetOrganisationPaginerOptimise,
                entitySearchParams: entitySearchParams,
                entityParams: entityParams,
                loadCallBack: vm.load
            };

            TableService.init(datas);
            vm.canShowResultCount=true;
            console.log(datas)
            // console.log('FSE',vm.itemSearch.pereUoId);
            // console.log('typeUO',vm.itemSearch.typeUO);

        }
        //**************************************************************************************
    }
})();
