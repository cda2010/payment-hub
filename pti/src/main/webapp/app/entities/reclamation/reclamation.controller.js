(function() {
    'use strict';
    angular
        .module('ptiApp')
        .controller('ReclamationController', ReclamationController);

    ReclamationController.$inject = ['$scope','TableService','$state', 'Reclamation','TypeReclamation','BASE_URL_API','$stateParams'];
    function ReclamationController($scope,TableService,$state, Reclamation, TypeReclamation,BASE_URL_API,$stateParams) {
        var vm = this;
        vm.title="Liste des reclamations";
        vm.consulting=$stateParams.consulting;
        //console.log($stateParams.consulting);
        vm.init=init;
        vm.chercher=chercher;
        //attributs de filtrage
        vm.itemSearch = {
            'titre':null,
            'etat':null,
            'qualiteReclamation':null,
            'telephoneAuteur':null,
            'dateCreation':null,
            'dateModification':null
        }
        vm.allData=[];
        vm.afficheFiltre=false;
        vm.afficherFiltre=afficherFiltre;
        function afficherFiltre(){
            vm.afficheFiltre=!vm.afficheFiltre;
            if(!vm.afficheFiltre){
                vm.itemSearch = {
                    'titre':null,
                    'etat':null,
                    'qualiteReclamation':null,
                    'telephoneAuteur':null,
                    'dateCreation':null,
                    'dateModification':null,
                }
                init();
            }

        }


        //valeurs de selections
        vm.typeReclamations=[];
        vm.etats=['','EN_INSTANCE', 'ATTRIBUE', 'RESOLU' , 'CLOS'];
        vm.qualiteAuteurs=['','AGENT', 'MARCHAND', 'CLIENT', 'ENTREPRISE'];
        vm.typeReclamations=TypeReclamation.query();

        init();
        function init(){
            var addItemData = {
                templateUrl: 'app/entities/reclamation/reclamation-dialog.html',
                controller: 'ReclamationDialogController',
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
                            typeReclamation: null,
                            etat: null,
                            qualiteActeur: true,
                            telephone: null,
                            document: null,
                            solution: null,
                            dateCreation: null,
                            dateModification: vm.id,
                            contenu: vm.niveau,
                        };
                    }
                }
            };

            var editItemData = {
                templateUrl: 'app/entities/reclamation/reclamation-dialog.html',
                controller: 'ReclamationDialogController',
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
                templateUrl: 'app/entities/reclamation/reclamation-dialog.html',
                controller: 'ReclamationDialogController',
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
                templateUrl: 'app/entities/reclamation/reclamation-delete-dialog.html',
                controller: 'ReclamationDeleteController',
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
                    titre: vm.itemSearch.titre,
                    etat:vm.itemSearch.etat,
                    qualiteReclamation:vm.itemSearch.qualiteReclamation,
                    telephoneAuteur:vm.itemSearch.telephoneAuteur,
                    dateCreation:vm.itemSearch.dateCreation,
                    dateModification:vm.itemSearch.dateModification,
                };
            };
            var datas = {
                scope: $scope,
                vm: vm,
                entity: Reclamation,
                dialogData: dialogData,
                entitySearch: Reclamation,
                entitySearchParams: entitySearchParams,
                entityParams: entityParams,
                loadCallBack: vm.load
            };
            TableService.init(datas);

        }

        function chercher(){
            console.log("Filtrage en cours ....");
           vm.datas=filtrer();
        }
        function  filtrer() {
             var dateCreationFiltre=new Date(vm.itemSearch.dateCreation);
             var dateModificationFiltre=new Date(vm.itemSearch.dateModification);
            var resultat=[];
            let i=0;
             for(i=0;i<vm.allData.length;i++){
                var dateCreationItem=new Date(vm.allData[i].dateHeureCreation);
                 var dateModificationItem=new Date(vm.allData[i].dateModification);
                 if(!vm.allData[i].telephone){
                    vm.allData[i].telephone="";
                }
                 if((! vm.itemSearch.titre ||vm.allData[i].typeReclamation.id==vm.itemSearch.titre.id) &&
                     (vm.allData[i].etat==vm.itemSearch.etat||!vm.itemSearch.etat) &&
                     (vm.allData[i].qualiteAuteur==vm.itemSearch.qualite||! vm.itemSearch.qualite)&&
                     (vm.allData[i].telephone.includes(vm.itemSearch.telephone)||! vm.itemSearch.telephone)
                     &&//comparaison de la date de creation
                     (!vm.itemSearch.dateCreation||(dateCreationItem.getMonth()==dateCreationFiltre.getMonth()&&
                         dateCreationItem.getDate()==dateCreationFiltre.getDate()&&
                         dateCreationItem.getFullYear()==dateCreationFiltre.getFullYear()))
                     &&//comparaison de la date de Modification
                    (!vm.itemSearch.dateModification||(dateModificationItem.getMonth()==dateModificationFiltre.getMonth()&&
                        dateModificationItem.getDate()==dateModificationFiltre.getDate()&&
                         dateModificationItem.getFullYear()==dateModificationFiltre.getFullYear()))
                     ){
                     resultat.push(vm.allData[i]);
                 }
             }
            return resultat;
        }

         function afficherFiltre(){
          vm.afficheFiltre=!vm.afficheFiltre;
            if(vm.allData.length<=0){
                vm.allData=Reclamation.query();
             }
            if (!vm.afficheFiltre) {
                 if(vm.allData.length>0){
                    vm.datas =Reclamation.query();
                }
                vm.itemSearch = {
                     'titre':null,
                    'etat':null,
                     'qualite':null,
                    'telephone':null,
                    'dateCreation':null,
                     'dateModification':null
                 }
             }
        }

    }
})();
