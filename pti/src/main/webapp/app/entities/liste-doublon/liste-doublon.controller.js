(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ListeDoublonController', ListeDoublonController);

    ListeDoublonController.$inject = ['$state', '$scope','$uibModal','RequestUtils','Notification','TableService','ListeDoublon','TypePiece'];

    function ListeDoublonController($state,$scope,$uibModal,RequestUtils,Notification,TableService ,ListeDoublon,TypePiece) {

        var vm = this;
        var j=0;
        vm.getDoublon=getDoublon;
        vm.clearSelect=clearSelect;
        vm.viewPersonneLien=viewPersonneLien;
        vm.selectEntity=selectEntity;
        vm.getValide=getValide;
        //vm.liste=null;
        vm.itemsPerPage = 20;
        vm.canShowResultCount=false;
        vm.flag=false;
        vm.itemSearch = {
            hash:false,
            typePieceId:null
        };
        vm.typepieces = TypePiece.query();
        var personne={
            listePersonne: null,
            nomClient: null,
            telephone: null,
            listePersonneId: null
        };

        function getDoublon() {
            console.log('Item Search',vm.itemSearch);
            console.log('rrrrr',vm.itemSearch.hash);
            // ListeDoublon.getDoublonListe({hash:vm.itemSearch.hash,typePieceId:vm.itemSearch.typePieceId},function (result) {
            //     console.log(result);
            //     vm.liste=result;
            // });
            init();
        }

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
                    "nom": vm.itemSearch.nom,
                    "hash": vm.itemSearch.hash,
                    "typePieceId": vm.itemSearch.typePieceId
                };
            };
            var datas = {
                scope: $scope,
                vm: vm,
                entity: ListeDoublon,
                entitySearch: ListeDoublon,
                dialogData: dialogData,
                entitySearchParams: entitySearchParams,
                entityParams: entityParams
            };
            TableService.init(datas);
            console.log('datas---Doublon',datas);
            vm.canShowResultCount = true;
        }

        function clearSelect() {
            vm.itemSearch = {
                nom:null,
                prenom:null,
                telephone:null,
                typePieceId:null
            };
        }

        function viewPersonneLien(lienId) {
            console.log('Great',lienId);
            $uibModal.open({
                templateUrl: 'app/entities/liste-doublon/personne-lien.html',
                controller: 'PersonneLienController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'lg',
                resolve: {
                    entity: function () {
                        return lienId;
                    }
                }
            }).result.then(function () {
                //init()
            }, function () {
            });
        }

        function selectEntity() {
            j++;
            console.log('ok',j);
            console.log('liste.isChecked');
            if(j>1){
                vm.flag=true;
            } else {
                vm.flag=false;
            }
        }


       // loadAll();

        function getValide() {
            console.log('-------------');
            vm.liste=[];
                for (var i = 0; i < vm.datas.length; i++) {
                    console.log('-1.1.1');
                    if (vm.datas[i].isChecked) {
                        console.log('t---',vm.datas[i]);
                        vm.liste.push(vm.datas[i].id);
                    }
                }
                //personne.listePersonne=vm.liste;
                personne.listePersonneId=vm.liste;
                personne.telephone='7895621';
                if(vm.liste.length>1){
                    console.log('ddd',personne);
                    RequestUtils.SaveDoublon.save(personne,function (result) {
                        console.log('liste',result);
                        Notification.success('Opération effectuée');
                    })
                } else {
                    Notification.error('Veillez selectionnez au moins 2');
                }
        }

        function loadAll () {
            ListeDiffusion.query({})

        }



    }
})();
